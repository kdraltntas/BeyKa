import java.io.*;
import java.util.*;

/**
 * BeyKa.java
 *
 * BeyKa programlama dili için ana çalıştırıcı sınıf.
 * Derlenen kaynak dosyasını (örnek: ornek1.bka) tokenize eder,
 * token listesini ve analiz sonuçlarını log dosyalarına yazar,
 * ardından programı çalıştırır ve çıktı ile hataları kaydeder.
 *
 * Çalıştırma: java BeyKa <ornekler/ornek1.bka>
 */
public class BeyKa {
    public static void main(String[] args) {
        // === Komut satırı argüman kontrolü ===
        if (args.length != 1) {
            System.out.println("Kullanım: java BeyKa <ornekler/ornek1.bka>");
            return;
        }

        // === Log klasörü hazırlığı ===
        File logDir = new File("logs");
        if (!logDir.exists()) logDir.mkdirs();

        // Token listesini tutacak
        List<BeyKaLexer.TokenInfo> tokenList = new ArrayList<>();

        // === 1. Tokenizasyon: Kaynak dosyadan tokenları çıkar ve logla ===
        try (BeyKaLexer lexer = new BeyKaLexer(args[0]);
             PrintWriter tokenWriter = new PrintWriter(new File(logDir, "tokens.txt"))) {

            BeyKaLexer.TokenInfo token;
            while ((token = lexer.nextToken()) != null) {
                tokenList.add(token);
                // Token bilgisini dosyaya yaz
                tokenWriter.printf("Token: %d\tLexeme: %s [satır %d, sütun %d]%n",
                        token.token, token.lexeme, token.line, token.column);
            }

        } catch (IOException e) {
            System.err.println("Dosya okuma hatası: " + e.getMessage());
            return;
        }

        // Token listesini ekrana yazdır
        System.out.println("=== TOKEN LİSTESİ ===");
        for (BeyKaLexer.TokenInfo t : tokenList) {
            System.out.printf("Token: %d\tLexeme: %s [satır %d, sütun %d]%n",
                    t.token, t.lexeme, t.line, t.column);
        }

        System.out.println("\n=== BEYKA PARSER BAŞLIYOR ===");

        // Çıktıyı hem ekrana hem dosyaya kaydetmek için stream ayarları
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream tempOut = new PrintStream(baos);

        try {
            /**
             * Tee OutputStream:
             * Terminale ve logs/output.txt'ye eşzamanlı çıktı gönderir.
             */
            PrintStream tee = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    tempOut.write(b);      // output.txt için
                    originalOut.write(b);  // terminal için
                }
            }, true);

            System.setOut(tee);
            // Parser'ı başlat ve programı çalıştır
            new BeyKaParser(tokenList).parseProgram();

        } catch (RuntimeException e) {
            // Beklenmedik hata olursa eski çıktı sistemine dön ve hatayı logla
            System.setOut(originalOut);
            try (PrintWriter errLog = new PrintWriter(new File(logDir, "errors.txt"))) {
                errLog.println(e.getMessage());
                System.err.println("Hata: " + e.getMessage());
                return;
            } catch (IOException io) {
                System.err.println("Hata dosyasına yazılamadı: " + io.getMessage());
            }
        }

        // Çıktıyı tekrar terminale döndür
        System.setOut(originalOut);

        // Program çıktısını dosyaya kaydet
        try (PrintWriter outLog = new PrintWriter(new File(logDir, "output.txt"))) {
            outLog.write(baos.toString());
        } catch (IOException e) {
            System.err.println("Çıktı dosyasına yazılamadı: " + e.getMessage());
        }

        System.out.println("BeyKa programı çalıştırıldı. Çıktılar 'logs/' klasörüne kaydedildi.");
    }
}
