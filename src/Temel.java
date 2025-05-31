import java.io.*;
import java.util.*;

public class Temel {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Kullanım: java Temel <ornekler/ornek1.cbü>");
            return;
        }

        File logDir = new File("logs");
        if (!logDir.exists()) logDir.mkdirs();

        List<CBULexer.TokenInfo> tokenList = new ArrayList<>();

        try (CBULexer lexer = new CBULexer(args[0]);
             PrintWriter tokenWriter = new PrintWriter(new File(logDir, "tokens.txt"))) {

            CBULexer.TokenInfo token;
            while ((token = lexer.nextToken()) != null) {
                tokenList.add(token);
                tokenWriter.printf("Token: %d\tLexeme: %s [satır %d, sütun %d]%n",
                        token.token, token.lexeme, token.line, token.column);
            }

        } catch (IOException e) {
            System.err.println("Dosya okuma hatası: " + e.getMessage());
            return;
        }

        System.out.println("=== TOKEN LİSTESİ ===");
        for (CBULexer.TokenInfo t : tokenList) {
            System.out.printf("Token: %d\tLexeme: %s [satır %d, sütun %d]%n",
                    t.token, t.lexeme, t.line, t.column);
        }

        System.out.println("\n=== PARSER BAŞLIYOR ===");

        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream tempOut = new PrintStream(baos);

        try {
            // Tee Output: Hem terminale hem dosya için OutputStream
            PrintStream tee = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    tempOut.write(b);      // output.txt için
                    originalOut.write(b);  // terminal için
                }
            }, true);

            System.setOut(tee);
            new CBUParser(tokenList).parseProgram();

        } catch (RuntimeException e) {
            System.setOut(originalOut);
            try (PrintWriter errLog = new PrintWriter(new File(logDir, "errors.txt"))) {
                errLog.println(e.getMessage());
                System.err.println("Hata: " + e.getMessage());
                return;
            } catch (IOException io) {
                System.err.println("Hata dosyasına yazılamadı: " + io.getMessage());
            }
        }

        System.setOut(originalOut);

        try (PrintWriter outLog = new PrintWriter(new File(logDir, "output.txt"))) {
            outLog.write(baos.toString());
        } catch (IOException e) {
            System.err.println("Çıktı dosyasına yazılamadı: " + e.getMessage());
        }

        System.out.println("Program çalıştırıldı. Çıktılar 'logs/' klasörüne kaydedildi.");
    }
}
