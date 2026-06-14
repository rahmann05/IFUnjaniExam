const prisma = require('../config/db');

exports.getMyClasses = async (req, res, next) => {
  try {
    const { userId, role } = req.user;
    let classes = [];

    if (role === 'DOSEN') {
      const dosen = await prisma.dosen.findUnique({ where: { userId } });
      if (!dosen) return res.status(404).json({ success: false, message: 'Data dosen tidak ditemukan' });
      
      classes = await prisma.kelas.findMany({
        where: { dosenId: dosen.id },
        include: { course: true, semester: true }
      });
    } else if (role === 'MAHASISWA') {
      const mahasiswa = await prisma.mahasiswa.findUnique({ where: { userId } });
      if (!mahasiswa) return res.status(404).json({ success: false, message: 'Data mahasiswa tidak ditemukan' });

      const enrollments = await prisma.kelasMahasiswa.findMany({
        where: { mahasiswaId: mahasiswa.id },
        include: { 
          kelas: {
            include: { course: true, semester: true, dosen: true }
          } 
        }
      });
      classes = enrollments.map(e => e.kelas);
    }

    res.json({ success: true, data: classes });
  } catch (error) {
    next(error);
  }
};

exports.getClassDetail = async (req, res, next) => {
  try {
    const classId = parseInt(req.params.id);
    const { userId, role } = req.user;

    let includeClause = {
      course: true,
      semester: true,
      dosen: true,
      mahasiswa: {
        include: {
          mahasiswa: true
        }
      }
    };

    if (role === 'MAHASISWA') {
      const student = await prisma.mahasiswa.findUnique({ where: { userId } });
      if (student) {
        includeClause.exams = {
          include: {
            attempts: {
              where: { mahasiswaId: student.id }
            }
          }
        };
      } else {
        includeClause.exams = true;
      }
    } else {
      includeClause.exams = true;
    }

    const kelas = await prisma.kelas.findUnique({
      where: { id: classId },
      include: includeClause
    });

    if (!kelas) {
      return res.status(404).json({ success: false, message: 'Kelas tidak ditemukan' });
    }

    res.json({ success: true, data: kelas });
  } catch (error) {
    next(error);
  }
};

exports.joinClass = async (req, res, next) => {
  try {
    const { classId, classCode } = req.body;
    const { userId, role } = req.user;

    if (role !== 'MAHASISWA') {
      return res.status(403).json({ success: false, message: 'Hanya mahasiswa yang dapat bergabung.' });
    }

    const mahasiswa = await prisma.mahasiswa.findUnique({ where: { userId } });
    if (!mahasiswa) {
      return res.status(404).json({ success: false, message: 'Data mahasiswa tidak ditemukan.' });
    }

    let kelas;
    if (classCode) {
      kelas = await prisma.kelas.findUnique({ where: { code: classCode } });
    } else if (classId) {
      kelas = await prisma.kelas.findUnique({ where: { id: parseInt(classId) } });
    }

    if (!kelas) {
      return res.status(404).json({ success: false, message: 'ID Kelas salah / kelas tidak ditemukan.' });
    }

    const existingEnrollment = await prisma.kelasMahasiswa.findFirst({
      where: {
        classId: kelas.id,
        mahasiswaId: mahasiswa.id
      }
    });

    if (existingEnrollment) {
      return res.status(400).json({ success: false, message: 'Anda sudah terdaftar di kelas ini.' });
    }

    await prisma.kelasMahasiswa.create({
      data: {
        classId: kelas.id,
        mahasiswaId: mahasiswa.id
      }
    });

    res.json({ success: true, message: 'Berhasil bergabung ke kelas.' });
  } catch (error) {
    next(error);
  }
};
