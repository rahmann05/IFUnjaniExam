require('dotenv').config();
const express = require('express');
const cors = require('cors');
const compression = require('compression');
const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');

const prisma = new PrismaClient();
const app = express();

app.use(compression());
app.use(cors());
app.use(express.json());

const JWT_SECRET = process.env.JWT_SECRET || 'rahasia_unjani_super_aman_123';

// Auth middleware
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1]; // Format: Bearer <token>

  if (!token) {
    return res.status(401).json({ success: false, message: 'Akses ditolak. Token tidak ditemukan.' });
  }

  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ success: false, message: 'Token tidak valid atau kadaluarsa.' });
    req.user = user; // Berisi payload { userId, role, profileId }
    next();
  });
}

// Routes
app.post('/api/login', async (req, res) => {
  const { username, password } = req.body;

  if (!username || !password) {
    return res.status(400).json({ success: false, message: 'Username dan password wajib diisi' });
  }

  try {
    const user = await prisma.user.findUnique({
      where: { username: username },
      include: {
        mahasiswa: true,
        dosen: true
      }
    });

    if (!user) {
      return res.status(401).json({ success: false, message: 'Username tidak ditemukan' });
    }
    const validPassword = await bcrypt.compare(password, user.password);
    if (!validPassword) {
      return res.status(401).json({ success: false, message: 'Password salah' });
    }

    const profileId = user.role === 'MAHASISWA' ? user.mahasiswa?.id : user.dosen?.id;
    const token = jwt.sign(
      { userId: user.id, username: user.username, role: user.role, profileId },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    return res.status(200).json({
      success: true,
      message: 'Login berhasil',
      token: token,
      data: {
        id: user.id,
        username: user.username,
        role: user.role,
        profile: user.role === 'MAHASISWA' ? user.mahasiswa : user.dosen
      }
    });
  } catch (error) {
    console.error('Error saat login:', error);
    return res.status(500).json({ success: false, message: 'Terjadi kesalahan pada server' });
  }
});

app.get('/api/exams', authenticateToken, async (req, res) => {
  try {
    const exams = await prisma.exam.findMany({
      include: {
        kelas: {
          include: {
            dosen: true,
            course: true
          }
        }
      }
    });

    return res.status(200).json({
      success: true,
      data: exams
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal mengambil data ujian' });
  }
});

app.get('/api/exams/:id/questions', authenticateToken, async (req, res) => {
  const examId = parseInt(req.params.id);
  
  try {
    const exam = await prisma.exam.findUnique({
      where: { id: examId },
      include: {
        questions: {
          select: {
            id: true,
            text: true,
            imageUrl: true,
            marks: true,
            type: true,
            examId: true,
            createdAt: true,
            updatedAt: true,
            options: {
              select: {
                id: true,
                text: true // Sembunyikan isCorrect
              }
            }
          }
        }
      }
    });

    if (!exam) return res.status(404).json({ success: false, message: 'Ujian tidak ditemukan' });

    return res.status(200).json({
      success: true,
      data: exam
    });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal mengambil soal ujian' });
  }
});

app.post('/api/exams/:id/submit', authenticateToken, async (req, res) => {
  const examId = parseInt(req.params.id);
  const { answers } = req.body; 
  const mahasiswaId = req.user.profileId;

  if (req.user.role !== 'MAHASISWA') {
    return res.status(403).json({ success: false, message: 'Hanya mahasiswa yang bisa submit ujian' });
  }

  if (!answers || !Array.isArray(answers)) {
    return res.status(400).json({ success: false, message: 'Format jawaban tidak valid' });
  }

  try {
    const questions = await prisma.question.findMany({
      where: { examId },
      include: { options: true }
    });

    let score = 0;
    let totalMarks = 0;
    const attemptAnswers = [];

    for (const q of questions) {
      totalMarks += q.marks;
      const studentAnswer = answers.find(a => a.questionId === q.id);
      
      if (studentAnswer) {
        if (q.type === 'MULTIPLE_CHOICE' && studentAnswer.selectedOptionId) {
          const selectedOpt = q.options.find(opt => opt.id === studentAnswer.selectedOptionId);
          if (selectedOpt && selectedOpt.isCorrect) {
            score += q.marks;
          }
          attemptAnswers.push({
            questionId: q.id,
            selectedOptionId: studentAnswer.selectedOptionId
          });
        } else if (q.type === 'ESSAY' && studentAnswer.essayAnswer) {
          if (q.correctEssayAnswer && studentAnswer.essayAnswer.trim().toLowerCase() === q.correctEssayAnswer.trim().toLowerCase()) {
            score += q.marks;
          }
          attemptAnswers.push({
            questionId: q.id,
            essayAnswer: studentAnswer.essayAnswer
          });
        }
      }
    }

    const finalScore = totalMarks > 0 ? (score / totalMarks) * 100 : 0;

    const attempt = await prisma.examAttempt.create({
      data: {
        examId: examId,
        mahasiswaId: mahasiswaId,
        score: finalScore,
        endTime: new Date(),
        answers: {
          create: attemptAnswers
        }
      }
    });

    return res.status(200).json({
      success: true,
      message: 'Ujian berhasil disubmit',
      data: { score: finalScore, attemptId: attempt.id }
    });

  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal memproses pengumpulan ujian' });
  }
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`🚀 Webservice berjalan di http://localhost:${PORT}`);
});
