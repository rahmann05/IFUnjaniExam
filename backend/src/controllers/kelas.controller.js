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
    const kelas = await prisma.kelas.findUnique({
      where: { id: classId },
      include: {
        course: true,
        semester: true,
        dosen: true,
        exams: true,
        mahasiswa: {
          include: {
            mahasiswa: true
          }
        }
      }
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
    const { classId } = req.body;
    const { userId, role } = req.user;

    if (role !== 'MAHASISWA') {
      return res.status(403).json({ success: false, message: 'Hanya mahasiswa yang dapat bergabung.' });
    }

    const mahasiswa = await prisma.mahasiswa.findUnique({ where: { userId } });
    if (!mahasiswa) {
      return res.status(404).json({ success: false, message: 'Data mahasiswa tidak ditemukan.' });
    }

    const kelas = await prisma.kelas.findUnique({ where: { id: parseInt(classId) } });
    if (!kelas) {
      return res.status(404).json({ success: false, message: 'Kelas tidak ditemukan.' });
    }

    await prisma.kelasMahasiswa.create({
      data: {
        classId: parseInt(classId),
        mahasiswaId: mahasiswa.id
      }
    });

    res.json({ success: true, message: 'Berhasil bergabung dengan kelas.' });
  } catch (error) {
    if (error.code === 'P2002') {
      return res.status(400).json({ success: false, message: 'Anda sudah terdaftar di kelas ini.' });
    }
    next(error);
  }
};
