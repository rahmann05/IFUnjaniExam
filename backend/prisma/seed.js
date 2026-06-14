require('dotenv').config();
const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcrypt');

const prisma = new PrismaClient();

const studentNames = [
  'Agus Setiawan', 'Budi Cahyono', 'Citra Lestari', 'Dedi Kurniawan', 'Eka Saputra',
  'Fajar Hidayat', 'Gita Permata', 'Hendra Wijaya', 'Indah Sari', 'Joko Susilo',
  'Kartika Putri', 'Lukman Hakim', 'Mega Utami', 'Naufal Rizqi', 'Olivia Widya',
  'Prabowo Subianto', 'Qori Aina', 'Rian Hidayat', 'Siti Aminah', 'Taufik Hidayat'
];

async function main() {
  console.log('🌱 Memulai pembersihan database...');

  // Hapus data lama agar bersih dan tidak melanggar constraint unique/foreign key
  await prisma.attemptAnswer.deleteMany({});
  await prisma.examAttempt.deleteMany({});
  await prisma.answerOption.deleteMany({});
  await prisma.question.deleteMany({});
  await prisma.examApprovalRequest.deleteMany({});
  await prisma.exam.deleteMany({});
  await prisma.kelasMahasiswa.deleteMany({});
  await prisma.kelas.deleteMany({});
  await prisma.semester.deleteMany({});
  await prisma.mataKuliah.deleteMany({});
  await prisma.dosen.deleteMany({});
  await prisma.mahasiswa.deleteMany({});
  await prisma.user.deleteMany({});

  console.log('🧹 Database dibersihkan. Memulai proses seeding...');

  // Hash password standar untuk semua user
  const hashedPassword = await bcrypt.hash('password123', 10);

  // 1. Buat 1 Admin
  const adminUser = await prisma.user.create({
    data: {
      username: 'admin',
      password: hashedPassword,
      role: 'ADMIN',
    },
  });
  console.log(`👤 Admin dibuat: ${adminUser.username}`);

  // 2. Buat 2 Dosen
  const dosenData = [
    { username: 'dosen1', nip: '198001012005011001', name: 'Ahmad Subagja, M.T.' },
    { username: 'dosen2', nip: '198805122015042002', name: 'Dr. Rina Wijaya, M.Kom.' }
  ];

  const dbDosens = [];
  for (const d of dosenData) {
    const userDosen = await prisma.user.create({
      data: {
        username: d.username,
        password: hashedPassword,
        role: 'DOSEN',
        dosen: {
          create: {
            nip: d.nip,
            name: d.name,
          }
        }
      },
      include: {
        dosen: true
      }
    });
    dbDosens.push(userDosen.dosen);
    console.log(`👨‍🏫 Dosen dibuat: ${d.name} (${d.nip})`);
  }

  // 3. Buat 20 Mahasiswa
  const dbMahasiswas = [];
  for (let i = 0; i < 20; i++) {
    const nimNumber = 20230001 + i;
    const nimString = nimNumber.toString();
    const name = studentNames[i];

    const userMahasiswa = await prisma.user.create({
      data: {
        username: nimString,
        password: hashedPassword,
        role: 'MAHASISWA',
        mahasiswa: {
          create: {
            nim: nimString,
            name: name,
          }
        }
      },
      include: {
        mahasiswa: true
      }
    });
    dbMahasiswas.push(userMahasiswa.mahasiswa);
    console.log(`🎓 Mahasiswa dibuat: ${name} (${nimString})`);
  }

  // 4. Buat Semester Aktif
  const semester = await prisma.semester.create({
    data: {
      name: 'Ganjil 2025/2026',
      isActive: true,
    }
  });
  console.log(`Semester dibuat: ${semester.name}`);

  // 5. Buat 3 Mata Kuliah
  const courseData = [
    { code: 'IF301', name: 'Pemrograman Mobile', sks: 3 },
    { code: 'IF302', name: 'Pemrograman Web', sks: 3 },
    { code: 'IF303', name: 'Basis Data', sks: 4 }
  ];

  const dbCourses = [];
  for (const c of courseData) {
    const course = await prisma.mataKuliah.create({
      data: c
    });
    dbCourses.push(course);
    console.log(`📖 Mata Kuliah dibuat: ${c.name} (${c.code})`);
  }

  // 6. Buat 3 Kelas
  // Kelas 1: IF-A (Pemrograman Mobile, Dosen 1)
  // Kelas 2: IF-B (Pemrograman Web, Dosen 2)
  // Kelas 3: IF-C (Basis Data, Dosen 1)
  const classData = [
    { name: 'IF-A', courseId: dbCourses[0].id, dosenId: dbDosens[0].id },
    { name: 'IF-B', courseId: dbCourses[1].id, dosenId: dbDosens[1].id },
    { name: 'IF-C', courseId: dbCourses[2].id, dosenId: dbDosens[0].id }
  ];

  const dbKelas = [];
  for (const c of classData) {
    const kelas = await prisma.kelas.create({
      data: {
        name: c.name,
        courseId: c.courseId,
        semesterId: semester.id,
        dosenId: c.dosenId,
      }
    });
    dbKelas.push(kelas);
    console.log(`Kelas dibuat: ${kelas.name}`);
  }

  // 7. Enrolling 20 Mahasiswa ke 3 Kelas
  // Kelas IF-A: Mahasiswa 1-12
  // Kelas IF-B: Mahasiswa 8-20
  // Kelas IF-C: Mahasiswa 1-7 dan 13-20
  console.log('Menghubungkan mahasiswa ke masing-masing kelas...');

  // Enroll IF-A
  for (let i = 0; i < 12; i++) {
    await prisma.kelasMahasiswa.create({
      data: {
        classId: dbKelas[0].id,
        mahasiswaId: dbMahasiswas[i].id,
      }
    });
  }

  // Enroll IF-B
  for (let i = 7; i < 20; i++) {
    await prisma.kelasMahasiswa.create({
      data: {
        classId: dbKelas[1].id,
        mahasiswaId: dbMahasiswas[i].id,
      }
    });
  }

  // Enroll IF-C
  const ifCMhsIndices = [0, 1, 2, 3, 4, 5, 6, 12, 13, 14, 15, 16, 17, 18, 19];
  for (const idx of ifCMhsIndices) {
    await prisma.kelasMahasiswa.create({
      data: {
        classId: dbKelas[2].id,
        mahasiswaId: dbMahasiswas[idx].id,
      }
    });
  }

  console.log('Seeding database selesai dengan sukses!');
  console.log('\nInformasi Akun Login (Password: password123):');
  console.log('1. Admin: username: admin');
  console.log('2. Dosen 1: username: dosen1');
  console.log('3. Dosen 2: username: dosen2');
  console.log('4. Mahasiswa: username 20230001 s.d. 20230020');
}

main()
  .catch((e) => {
    console.error('Error saat seeding:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
