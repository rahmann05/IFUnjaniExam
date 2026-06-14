const prisma = require('../config/db');

async function getExams(req, res) {
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
}

async function getExamQuestions(req, res) {
  const examId = parseInt(req.params.id);
  
  try {
    const exam = await prisma.exam.findUnique({
      where: { id: examId },
      include: {
        questions: {
          include: {
            options: {
              select: { id: true, text: true }
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
}

async function submitExam(req, res) {
  const examId = parseInt(req.params.id);
  const { answers } = req.body; 
  const mahasiswaId = req.user.profileId;

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
      
      if (studentAnswer && studentAnswer.selectedOptionId) {
        const selectedOpt = q.options.find(opt => opt.id === studentAnswer.selectedOptionId);
        if (selectedOpt && selectedOpt.isCorrect) {
          score += q.marks;
        }
        attemptAnswers.push({
          questionId: q.id,
          selectedOptionId: studentAnswer.selectedOptionId
        });
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
}

module.exports = { getExams, getExamQuestions, submitExam };
