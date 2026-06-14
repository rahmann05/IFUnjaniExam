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

module.exports = { getDashboardStats, getApprovalRequests, handleApproval };
