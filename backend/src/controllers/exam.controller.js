const prisma = require('../config/db');

async function getExams(req, res) {
  try {
    const { classId } = req.query;
    const whereClause = classId ? { classId: parseInt(classId) } : {};
    const exams = await prisma.exam.findMany({
      where: whereClause,
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

    const { title, description, classId, startTime, endTime, durationMinutes, category, weight, questions } = req.body;

    const exam = await prisma.exam.create({
      data: {
        title,
        description,
        classId,
        category: category || 'OTHER',
        weight: weight !== undefined ? parseFloat(weight) : 100.0,
        startTime: startTime ? new Date(startTime) : null,
        endTime: endTime ? new Date(endTime) : null,
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

async function getExamResults(req, res) {
  const examId = parseInt(req.params.id);
  
  try {
    const results = await prisma.examAttempt.findMany({
      where: { examId },
      include: {
        mahasiswa: true
      },
      orderBy: {
        score: 'desc'
      }
    });

    return res.status(200).json({ success: true, data: results });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal mengambil hasil ujian' });
  }
}

async function getAttemptDetail(req, res) {
  const attemptId = parseInt(req.params.id);
  
  try {
    const attempt = await prisma.examAttempt.findUnique({
      where: { id: attemptId },
      include: {
        mahasiswa: true,
        exam: true,
        answers: {
          include: {
            question: {
              include: {
                options: true
              }
            },
            selectedOption: true
          }
        }
      }
    });

    if (!attempt) return res.status(404).json({ success: false, message: 'Data attempt tidak ditemukan' });

    return res.status(200).json({ success: true, data: attempt });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal mengambil detail lembar jawaban' });
  }
}

async function deleteExam(req, res) {
  const examId = parseInt(req.params.id);
  
  try {
    const exam = await prisma.exam.findUnique({
      where: { id: examId },
      include: { attempts: true }
    });

    if (!exam) return res.status(404).json({ success: false, message: 'Ujian tidak ditemukan' });

    if (exam.attempts.length > 0) {
      // Check if there is an approved delete request
      const approvedRequest = await prisma.examApprovalRequest.findFirst({
        where: { examId, requestType: 'DELETE', status: 'APPROVED' }
      });

      if (!approvedRequest) {
        return res.status(403).json({ success: false, message: 'Ujian sudah dikerjakan mahasiswa. Perlu persetujuan Admin untuk menghapus.', requiresApproval: true });
      }
    }

    // Delete questions, options, etc. Cascade handles most if configured, but let's do it manually if needed, or rely on prisma onDelete: Cascade. 
    // Wait, prisma schema doesn't have onDelete Cascade for questions -> exam. 
    // We should delete manually.
    await prisma.answerOption.deleteMany({
      where: { question: { examId } }
    });
    
    await prisma.attemptAnswer.deleteMany({
      where: { question: { examId } }
    });
    
    await prisma.examAttempt.deleteMany({
      where: { examId }
    });

    await prisma.question.deleteMany({
      where: { examId }
    });
    
    await prisma.examApprovalRequest.deleteMany({
      where: { examId }
    });

    await prisma.exam.delete({
      where: { id: examId }
    });

    return res.status(200).json({ success: true, message: 'Ujian berhasil dihapus' });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal menghapus ujian' });
  }
}

async function requestApproval(req, res) {
  const examId = parseInt(req.params.id);
  const { requestType, reason } = req.body; // "EDIT" or "DELETE"
  
  try {
    if (req.user.role !== 'DOSEN') {
      return res.status(403).json({ success: false, message: 'Hanya Dosen yang bisa meminta persetujuan' });
    }

    const request = await prisma.examApprovalRequest.create({
      data: {
        examId,
        dosenId: req.user.profileId,
        requestType,
        reason
      }
    });

    return res.status(201).json({ success: true, message: 'Permintaan persetujuan berhasil dikirim ke Admin', data: request });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal mengirim permintaan persetujuan' });
  }
}

async function gradeAttempt(req, res) {
  const attemptId = parseInt(req.params.id);
  const { newScore } = req.body;
  
  try {
    if (req.user.role !== 'DOSEN') {
      return res.status(403).json({ success: false, message: 'Hanya Dosen yang bisa memberi nilai manual' });
    }

    const attempt = await prisma.examAttempt.update({
      where: { id: attemptId },
      data: { score: parseFloat(newScore) }
    });

    return res.status(200).json({ success: true, message: 'Nilai berhasil diperbarui', data: attempt });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal memperbarui nilai' });
  }
}

async function updateExamStatus(req, res) {
  const examId = parseInt(req.params.id);
  const { status } = req.body;
  
  try {
    if (req.user.role !== 'DOSEN') {
      return res.status(403).json({ success: false, message: 'Hanya Dosen yang bisa mengubah status' });
    }

    const validStatuses = ['PUBLISHED', 'ONGOING', 'FINISHED'];
    if (!validStatuses.includes(status)) {
      return res.status(400).json({ success: false, message: 'Status tidak valid' });
    }

    const dataToUpdate = { status };
    if (status === 'ONGOING') {
      const now = new Date();
      dataToUpdate.startTime = now;
      const examObj = await prisma.exam.findUnique({ where: { id: examId } });
      if (examObj) {
        dataToUpdate.endTime = new Date(now.getTime() + examObj.durationMinutes * 60000);
      }
    } else if (status === 'FINISHED') {
      dataToUpdate.endTime = new Date();
    }

    const exam = await prisma.exam.update({
      where: { id: examId },
      data: dataToUpdate
    });

    return res.status(200).json({ success: true, message: `Status ujian menjadi ${status}`, data: exam });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Gagal mengubah status ujian' });
  }
}

module.exports = { getExams, getExamQuestions, submitExam, createExam, getExamResults, getAttemptDetail, deleteExam, requestApproval, gradeAttempt, updateExamStatus };
