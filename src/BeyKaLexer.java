import java.io.*;
import java.util.*;

/**
 * BeyKaLexer.java
 *
 * BeyKa programlama dili için temel Lexical Analyzer (tokenizer) sınıfıdır.
 * Girilen kaynak dosyadan tokenları çıkarır, satır ve sütun bilgisi ile birlikte döndürür.
 * Türkçe karakter desteği ve satır içi yorum satırı (//) algılaması mevcuttur.
 */
public class BeyKaLexer implements AutoCloseable {

    // Okuma işlemleri için reader ve satır kontrolü
    private BufferedReader reader;
    private String line;
    private int pos;
    private int lineNumber = 0;

    // Desteklenen anahtar kelimeler
    private Set<String> keywords = new HashSet<>(Arrays.asList(
            "tamsayı", "ondalikli", "kelime", "yaz", "ise", "değilse",
            "döngü", "durdur", "devam", "eğer", "fonksiyon", "dön"
    ));

    /**
     * BeyKaLexer oluşturucu. Dosya yolunu alır ve dosyayı okumaya hazırlar.
     */
    public BeyKaLexer(String filename) throws IOException {
        reader = new BufferedReader(new FileReader(filename));
        line = "";
        pos = 0;
    }

    /**
     * Boşluk karakterlerini atlar.
     */
    private void skipWhitespace() {
        while (pos < line.length() && Character.isWhitespace(line.charAt(pos))) {
            pos++;
        }
    }

    /**
     * Bir sonraki tokenı okur ve TokenInfo olarak döndürür.
     * Satır sonuna gelindiğinde yeni satır okur, dosya bittiğinde null döner.
     */
    public TokenInfo nextToken() throws IOException {
        while (true) {
            // Satır bitti, yenisini oku
            if (pos >= line.length()) {
                line = reader.readLine();
                if (line == null) return null; // Dosya bitti
                lineNumber++;
                pos = 0;
                continue;
            }

            // Yorum satırı desteği: // sonrası atlanır
            int commentIdx = line.indexOf("//");
            if (commentIdx != -1) {
                line = line.substring(0, commentIdx);
            }

            skipWhitespace();
            if (pos >= line.length()) {
                line = "";
                pos = 0;
                continue;
            }

            char ch = line.charAt(pos);
            int startPos = pos;

            // --- String (tırnak içinde) desteği ---
            if (ch == '"') {
                pos++;
                StringBuilder sb = new StringBuilder();
                boolean closed = false;
                while (true) {
                    if (pos >= line.length()) {
                        line = reader.readLine();
                        if (line == null) break;
                        lineNumber++;
                        pos = 0;
                        sb.append('\n');
                        continue;
                    }
                    char c = line.charAt(pos);
                    if (c == '"') {
                        closed = true;
                        pos++;
                        break;
                    }
                    sb.append(c);
                    pos++;
                }
                if (!closed) {
                    // Hatalı string, kapanmamış
                    return new TokenInfo(Token.DIGER, sb.toString(), lineNumber, startPos);
                }
                return new TokenInfo(Token.STRING, sb.toString(), lineNumber, startPos);
            }

            // --- Operatör ve sembol ayrıştırma ---
            if (ch == '=') {
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '=') {
                    pos += 2;
                    return new TokenInfo(Token.ESIT_ESIT, "==", lineNumber, startPos);
                }
                pos++;
                return new TokenInfo(Token.ESITTIR, "=", lineNumber, startPos);
            } else if (ch == '!') {
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '=') {
                    pos += 2;
                    return new TokenInfo(Token.ESIT_DEGIL, "!=", lineNumber, startPos);
                }
                pos++;
                return new TokenInfo(Token.DIGER, "!", lineNumber, startPos);
            } else if (ch == '<') {
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '=') {
                    pos += 2;
                    return new TokenInfo(Token.KUCUK_ESIT, "<=", lineNumber, startPos);
                }
                pos++;
                return new TokenInfo(Token.KUCUK, "<", lineNumber, startPos);
            } else if (ch == '>') {
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '=') {
                    pos += 2;
                    return new TokenInfo(Token.BUYUK_ESIT, ">=", lineNumber, startPos);
                }
                pos++;
                return new TokenInfo(Token.BUYUK, ">", lineNumber, startPos);
            } else if (ch == '&') {
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '&') {
                    pos += 2;
                    return new TokenInfo(Token.VE, "&&", lineNumber, startPos);
                }
                pos++;
                return new TokenInfo(Token.DIGER, "&", lineNumber, startPos);
            } else if (ch == '|') {
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '|') {
                    pos += 2;
                    return new TokenInfo(Token.VEYA, "||", lineNumber, startPos);
                }
                pos++;
                return new TokenInfo(Token.DIGER, "|", lineNumber, startPos);
            } else if (ch == '+') {
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '+') {
                    pos += 2;
                    return new TokenInfo(Token.ARTTIR, "++", lineNumber, startPos);
                }
                pos++;
                return new TokenInfo(Token.TOPLA, "+", lineNumber, startPos);
            } else if (ch == '-') {
                if (pos + 1 < line.length() && line.charAt(pos + 1) == '-') {
                    pos += 2;
                    return new TokenInfo(Token.AZALT, "--", lineNumber, startPos);
                }
                pos++;
                return new TokenInfo(Token.CIKAR, "-", lineNumber, startPos);
            } else if (ch == '*') {
                pos++;
                return new TokenInfo(Token.CARP, "*", lineNumber, startPos);
            } else if (ch == '/') {
                pos++;
                return new TokenInfo(Token.BOL, "/", lineNumber, startPos);
            } else if (ch == '%') {
                pos++;
                return new TokenInfo(Token.MOD, "%", lineNumber, startPos);
            } else if (ch == '(') {
                pos++;
                return new TokenInfo(Token.PARANTEZ_AC, "(", lineNumber, startPos);
            } else if (ch == ')') {
                pos++;
                return new TokenInfo(Token.PARANTEZ_KAPA, ")", lineNumber, startPos);
            } else if (ch == '{') {
                pos++;
                return new TokenInfo(Token.SURET_AC, "{", lineNumber, startPos);
            } else if (ch == '}') {
                pos++;
                return new TokenInfo(Token.SURET_KAPA, "}", lineNumber, startPos);
            } else if (ch == ';') {
                pos++;
                return new TokenInfo(Token.NOKTALI_VIRGUL, ";", lineNumber, startPos);
            } else if (ch == ',') {
                pos++;
                return new TokenInfo(Token.VIRGUL, ",", lineNumber, startPos);
            } else if (ch == '.') {
                pos++;
                return new TokenInfo(Token.NOKTA, ".", lineNumber, startPos);
            }

            // --- Anahtar kelime veya değişken/identifier ayrıştırma ---
            if (Character.isLetter(ch) || isTurkishLetter(ch) || ch == '_') {
                int start = pos;
                while (pos < line.length() &&
                        (Character.isLetterOrDigit(line.charAt(pos)) || isTurkishLetter(line.charAt(pos)) || line.charAt(pos) == '_')) {
                    pos++;
                }
                String word = line.substring(start, pos);

                if (keywords.contains(word)) {
                    int keywordToken = switch (word) {
                        case "tamsayı"   -> Token.TAMSAYI;
                        case "ondalikli" -> Token.ONDALIKLI;
                        case "kelime"    -> Token.KELIME;
                        case "yaz"       -> Token.YAZ;
                        case "ise"       -> Token.ISE;
                        case "değilse"   -> Token.DEGILSE;
                        case "döngü"     -> Token.DONGU;
                        case "durdur"    -> Token.DURDUR;
                        case "devam"     -> Token.DEVAM;
                        case "eğer"      -> Token.EGER;
                        case "fonksiyon" -> Token.FONKSIYON;
                        case "dön"       -> Token.DON;
                        default          -> Token.DEGISKEN;
                    };
                    return new TokenInfo(keywordToken, word, lineNumber, start);
                } else {
                    // Değişken adı
                    return new TokenInfo(Token.DEGISKEN, word, lineNumber, start);
                }
            }

            // --- Sayı (tamsayı veya ondalıklı) ayrıştırma ---
            if (Character.isDigit(ch)) {
                int start = pos;
                boolean hasDot = false;
                while (pos < line.length() && (Character.isDigit(line.charAt(pos)) || (!hasDot && line.charAt(pos) == '.'))) {
                    if (line.charAt(pos) == '.') hasDot = true;
                    pos++;
                }
                String number = line.substring(start, pos);
                return new TokenInfo(Token.SAYI, number, lineNumber, start);
            }

            // --- Tanınmayan karakterler ---
            pos++;
            return new TokenInfo(Token.DIGER, String.valueOf(ch), lineNumber, startPos);
        }
    }

    /**
     * Türkçe karakter kontrolü
     */
    private boolean isTurkishLetter(char ch) {
        return "ğĞşŞöÖüÜçÇıİ".indexOf(ch) >= 0;
    }

    /**
     * Kaynak dosyayı kapatır.
     */
    @Override
    public void close() throws IOException {
        reader.close();
    }

    /**
     * TokenInfo iç sınıfı: her tokenin tipini, içeriğini ve konumunu tutar.
     */
    public static class TokenInfo {
        public int token;
        public String lexeme;
        public int line;
        public int column;

        public TokenInfo(int token, String lexeme, int line, int column) {
            this.token = token;
            this.lexeme = lexeme;
            this.line = line;
            this.column = column;
        }
    }
}
