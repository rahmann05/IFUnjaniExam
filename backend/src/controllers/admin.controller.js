const prisma = require('../config/db');

async function getDashboardStats(req, res) {
  try {
    const totalDosen = await prisma.dosen.count();
    const totalMahasiswa = await prisma.mahasiswa.count();
    const totalKelas = await prisma.kelas.count();
    const pendingRequests = await prisma.examApprovalRequest.count({ where: { status: 'PENDING' } });

    res.json({
      success: true,
      data: { totalDosen, totalMahasiswa, totalKelas, pendingRequests }
    });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal memuat statistik admin' });
  }
}

async function getApprovalRequests(req, res) {
  try {
    const requests = await prisma.examApprovalRequest.findMany({
      where: { status: 'PENDING' },
      include: {
        exam: true,
        dosen: true
      }
    });
    res.json({ success: true, data: requests });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal memuat permintaan' });
  }
}

async function handleApproval(req, res) {
  try {
    const { id } = req.params;
    const { action } = req.body; // "APPROVE" or "REJECT"

    const status = action === 'APPROVE' ? 'APPROVED' : 'REJECTED';
    
    await prisma.examApprovalRequest.update({
      where: { id: parseInt(id) },
      data: { status }
    });

    res.json({ success: true, message: `Permintaan berhasil di-${status.toLowerCase()}` });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal memproses persetujuan' });
  }
}

// ================= USER MANAGEMENT (DOSEN & MAHASISWA) =================
async function getUsers(req, res) {
  try {
    const { role } = req.query; // DOSEN or MAHASISWA
    const users = await prisma.user.findMany({
      where: role ? { role } : {},
      include: { dosen: true, mahasiswa: true },
      orderBy: { id: 'desc' }
    });
    res.json({ success: true, data: users });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal memuat pengguna' });
  }
}

const bcrypt = require('bcrypt');

async function createUser(req, res) {
  try {
    const { username, password, role, name, nip, nim } = req.body;
    const hashedPassword = await bcrypt.hash(password, 10);
    
    const user = await prisma.user.create({
      data: {
        username,
        password: hashedPassword,
        role,
        ...(role === 'DOSEN' ? { dosen: { create: { name, nip } } } : {}),
        ...(role === 'MAHASISWA' ? { mahasiswa: { create: { name, nim } } } : {})
      },
      include: { dosen: true, mahasiswa: true }
    });
    res.status(201).json({ success: true, message: 'Pengguna berhasil ditambahkan', data: user });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal menambah pengguna (mungkin username/NIP/NIM duplikat)' });
  }
}

async function deleteUser(req, res) {
  try {
    const { id } = req.params;
    const userId = parseInt(id);
    
    const user = await prisma.user.findUnique({ where: { id: userId }, include: { dosen: true, mahasiswa: true } });
    if (!user) return res.status(404).json({ success: false, message: 'Pengguna tidak ditemukan' });

    if (user.role === 'DOSEN' && user.dosen) await prisma.dosen.delete({ where: { id: user.dosen.id } });
    if (user.role === 'MAHASISWA' && user.mahasiswa) await prisma.mahasiswa.delete({ where: { id: user.mahasiswa.id } });
    
    await prisma.user.delete({ where: { id: userId } });
    
    res.json({ success: true, message: 'Pengguna berhasil dihapus' });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal menghapus pengguna (masih terikat dengan data kelas/ujian)' });
  }
}

// ================= CLASS MANAGEMENT =================
async function getClasses(req, res) {
  try {
    const classes = await prisma.kelas.findMany({
      include: { course: true, semester: true, dosen: true },
      orderBy: { id: 'desc' }
    });
    res.json({ success: true, data: classes });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal memuat kelas' });
  }
}

async function createClass(req, res) {
  try {
    const { name, courseId, semesterId, dosenId } = req.body;
    const kelas = await prisma.kelas.create({
      data: { name, courseId, semesterId, dosenId },
      include: { course: true, semester: true, dosen: true }
    });
    res.status(201).json({ success: true, message: 'Kelas berhasil dibuat', data: kelas });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal membuat kelas' });
  }
}

async function deleteClass(req, res) {
  try {
    await prisma.kelas.delete({ where: { id: parseInt(req.params.id) } });
    res.json({ success: true, message: 'Kelas berhasil dihapus' });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal menghapus kelas' });
  }
}

async function addMahasiswaToClass(req, res) {
  try {
    const classId = parseInt(req.params.id);
    const { mahasiswaId } = req.body;
    
    await prisma.kelasMahasiswa.create({
      data: {
        classId: classId,
        mahasiswaId: parseInt(mahasiswaId)
      }
    });
    res.json({ success: true, message: 'Mahasiswa berhasil ditambahkan ke kelas' });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal menambahkan mahasiswa ke kelas (Mungkin sudah terdaftar)' });
  }
}

async function updateClassDosen(req, res) {
  try {
    const classId = parseInt(req.params.id);
    const { dosenId } = req.body;
    
    await prisma.kelas.update({
      where: { id: classId },
      data: { dosenId: parseInt(dosenId) }
    });
    res.json({ success: true, message: 'Dosen berhasil diubah' });
  } catch (error) {
    res.status(500).json({ success: false, message: 'Gagal mengubah dosen kelas' });
  }
}

module.exports = { 
  getDashboardStats, getApprovalRequests, handleApproval,
  getUsers, createUser, deleteUser,
  getClasses, createClass, deleteClass, addMahasiswaToClass, updateClassDosen
};
