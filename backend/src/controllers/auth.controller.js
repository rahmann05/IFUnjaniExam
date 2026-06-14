const prisma = require('../config/db');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { JWT_SECRET } = require('../config/env');

async function login(req, res) {
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
}

async function changePassword(req, res) {
  try {
    const { oldPassword, newPassword } = req.body;
    const userId = req.user.userId;

    const user = await prisma.user.findUnique({ where: { id: userId } });
    if (!user) return res.status(404).json({ success: false, message: 'User tidak ditemukan' });

    const isMatch = await bcrypt.compare(oldPassword, user.password);
    if (!isMatch) return res.status(400).json({ success: false, message: 'Password lama salah' });

    const hashedPassword = await bcrypt.hash(newPassword, 10);
    await prisma.user.update({
      where: { id: userId },
      data: { password: hashedPassword }
    });

    return res.status(200).json({ success: true, message: 'Password berhasil diubah' });
  } catch (error) {
    console.error(error);
    return res.status(500).json({ success: false, message: 'Terjadi kesalahan saat mengubah password' });
  }
}

module.exports = { login, changePassword };
