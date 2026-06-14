package com.rahman.ifunjaniexam.network;

public class Config {
    // Gunakan 10.0.2.2 untuk emulator Android agar bisa mengakses localhost (server.js lokal).
    // Jika Anda mengetes di HP fisik (bukan emulator), ganti 10.0.2.2 dengan IP Address laptop Anda (misal: 192.168.x.x).
    // Jika ingin kembali ke Vercel, cukup uncomment baris di bawah ini dan comment baris lokal.
    
    // public static final String BASE_URL = "http://10.0.2.2:3000/api";
    public static final String BASE_URL = "https://if-unjani-exam-api.vercel.app/api";
}
