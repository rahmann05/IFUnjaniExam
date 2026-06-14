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
            options: true
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
  const { answers, score } = req.body; 
  const mahasiswaId = req.user.profileId;

  if (!answers || !Array.isArray(answers) || score === undefined) {
    return res.status(400).json({ success: false, message: 'Format jawaban atau score tidak valid' });
  }

  try {
    const attemptAnswers = answers.map(a => ({
      questionId: a.questionId,
      selectedOptionId: a.selectedOptionId || null,
      essayAnswer: a.essayAnswer || null
    }));

    const attempt = await prisma.examAttempt.create({
      data: {
        examId: examId,
        mahasiswaId: mahasiswaId,
        score: parseFloat(score),
        endTime: new Date(),
        answers: {
          create: attemptAnswers
        }
      }
    });

    return res.status(200).json({
      success: true,
      message: 'Ujian berhasil disubmit',
      data: { score: attempt.score, attemptId: attempt.id }
    });

  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal memproses pengumpulan ujian' });
  }
}

async function createExam(req, res) {
  try {
    if (req.user.role !== 'DOSEN') {
      return res.status(403).json({ success: false, message: 'Hanya Dosen yang bisa membuat ujian' });
    }

    const { title, description, classId, startTime, endTime, durationMinutes, questions } = req.body;

    const exam = await prisma.exam.create({
      data: {
        title,
        description,
        classId,
        startTime: new Date(startTime),
        endTime: new Date(endTime),
        durationMinutes,
        questions: {
          create: questions.map(q => {
            const isEssay = q.type === 'ESSAY';
            return {
              text: q.text,
              imageUrl: q.imageUrl,
              marks: q.marks || 1,
              type: q.type || 'MULTIPLE_CHOICE',
              correctEssayAnswer: isEssay ? q.correctEssayAnswer : null,
              options: isEssay ? undefined : {
                create: q.options.map(opt => ({
                  text: opt.text,
                  isCorrect: opt.isCorrect
                }))
              }
            };
          })
        }
      }
    });

    return res.status(201).json({ success: true, message: 'Ujian berhasil dibuat', data: exam });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal membuat ujian' });
  }
}

module.exports = { getExams, getExamQuestions, submitExam, createExam };
