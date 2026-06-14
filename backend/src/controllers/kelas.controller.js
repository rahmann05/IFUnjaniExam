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
