import java.io.*;
import java.util.*;

public class CBULexer implements AutoCloseable {
    private BufferedReader reader;
    private String line;
    private int pos;
    private int lineNumber = 0;

    private Set<String> keywords = new HashSet<>(Arrays.asList(
            "tamsayı", "ondalikli", "kelime", "yaz", "ise", "değilse",
            "döngü", "durdur", "devam", "eğer", "fonksiyon", "dön"
    ));

    public CBULexer(String filename) throws IOException {
        reader = new BufferedReader(new FileReader(filename));
        line = "";
        pos = 0;
    }

    private void skipWhitespace() {
        while (pos < line.length() && Character.isWhitespace(line.charAt(pos))) {
            pos++;
        }
    }

    public TokenInfo nextToken() throws IOException {
        while (true) {
            if (pos >= line.length()) {
                line = reader.readLine();
                if (line == null) return null;
                lineNumber++;
                pos = 0;
                continue;
            }

            // YORUM SATIRI DESTEĞİ: Satırda // varsa oradan sonrası atlanır
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
                    return new TokenInfo(Token.DIGER, sb.toString(), lineNumber, startPos);
                }

                return new TokenInfo(Token.STRING, sb.toString(), lineNumber, startPos);
            }

            // Operatör ve semboller (devamı)
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
                    return new TokenInfo(Token.DEGISKEN, word, lineNumber, start);
                }
            }

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

            pos++;
            return new TokenInfo(Token.DIGER, String.valueOf(ch), lineNumber, startPos);
        }
    }

    private boolean isTurkishLetter(char ch) {
        return "ğĞşŞöÖüÜçÇıİ".indexOf(ch) >= 0;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }

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
