require('dotenv').config();
const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcrypt');

// Inisialisasi Prisma standar (Node.js)
const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Memulai proses seeding...');

  // Hash password agar aman
  const hashedPassword = await bcrypt.hash('password123', 10);

  // 1. Buat User Dosen
  await prisma.user.upsert({
    where: { username: 'dosen_unjani' },
    update: { password: hashedPassword }, // Update password dengan hash baru
    create: {
      username: 'dosen_unjani',
      password: hashedPassword,
      role: 'DOSEN',
      dosen: {
        create: {
          nip: '198001012005011001',
          name: 'Prof. Budi Raharjo',
        }
      }
    },
  });

  // 2. Buat User Mahasiswa
  await prisma.user.upsert({
    where: { username: '20230001' },
    update: { password: hashedPassword },
    create: {
      username: '20230001',
      password: hashedPassword,
      role: 'MAHASISWA',
      mahasiswa: {
        create: {
          nim: '20230001',
          name: 'Agus Setiawan',
        }
      }
    },
  });

  console.log('Seeding data awal berhasil! Anda bisa mencoba login dengan username: 20230001, password: password123');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
