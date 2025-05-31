import java.time.LocalDate;
import java.util.*;
import java.nio.file.*;

/**
 * CBUParser.java
 *
 * CBU Türkçe programlama dili için recursive descent parser ve interpreter.
 * Fonksiyon tanımlama/çağırma, değişken işlemleri, koşullu ifadeler, döngüler ve temel tip kontrolü içerir.
 * Token listesini (CBULexer.TokenInfo) alır ve programı yorumlayarak çıktıyı üretir.
 */
public class CBUParser {
    /** Kullanıcı tanımlı fonksiyonları tutan iç sınıf. */
    private static class Function {
        List<String> parametreler;
        List<CBULexer.TokenInfo> govde;
        Function(List<String> parametreler, List<CBULexer.TokenInfo> govde) {
            this.parametreler = parametreler;
            this.govde = govde;
        }
    }

    /** Fonksiyondan erken çıkışı simüle eden özel exception. */
    private static class ReturnValue extends RuntimeException {
        Object value;
        ReturnValue(Object value) { this.value = value; }
    }

    private final List<CBULexer.TokenInfo> tokens; // Token akışı
    private int pos = 0; // Şu anki token pozisyonu
    private final List<String> errors = new ArrayList<>(); // Toplanan hata mesajları

    /** Desteklenen veri tipleri */
    private enum Type { TAMSAYI, ONDALIKLI, KELIME }

    private final Map<String,Type>      symbolTable     = new HashMap<>(); // Değişken tip tablosu
    private final Map<String,Object>    memory          = new HashMap<>(); // Değişken değerleri
    private final Map<String,Function>  fonksiyonTablosu = new HashMap<>(); // Fonksiyonlar

    private static final int MAX_LOOP_COUNT = 100; // Sonsuz döngü koruması

    /**
     * Token listesinden parser nesnesi oluşturur.
     */
    public CBUParser(List<CBULexer.TokenInfo> tokens) {
        this.tokens = tokens;
    }

    /**
     * Tüm fonksiyon tanımlarını baştan bulup fonksiyon tablosuna ekler.
     * (Parser'ın ilk geçişi)
     */
    public void ilkPassFonksiyonlariKaydet() {
        int tmpPos = 0;
        while (tmpPos < tokens.size()) {
            CBULexer.TokenInfo cur = tokens.get(tmpPos);
            if (cur.token == Token.FONKSIYON) {
                int fnPos = tmpPos;
                tmpPos++; // fonksiyon adı
                String fname = tokens.get(tmpPos).lexeme;
                tmpPos++; // (
                List<String> params = new ArrayList<>();
                if (tokens.get(tmpPos).token == Token.PARANTEZ_AC) {
                    tmpPos++;
                    while (tokens.get(tmpPos).token != Token.PARANTEZ_KAPA) {
                        if (tokens.get(tmpPos).token == Token.DEGISKEN) {
                            params.add(tokens.get(tmpPos).lexeme);
                            tmpPos++;
                            if (tokens.get(tmpPos).token == Token.VIRGUL) tmpPos++;
                        } else break;
                    }
                    tmpPos++; // )
                }
                if (tokens.get(tmpPos).token == Token.SURET_AC) {
                    int bodyStart = tmpPos + 1;
                    int depth = 1;
                    tmpPos++;
                    while (tmpPos < tokens.size() && depth > 0) {
                        if (tokens.get(tmpPos).token == Token.SURET_AC) depth++;
                        else if (tokens.get(tmpPos).token == Token.SURET_KAPA) depth--;
                        tmpPos++;
                    }
                    int bodyEnd = tmpPos - 1;
                    fonksiyonTablosu.put(fname,
                            new Function(params, new ArrayList<>(tokens.subList(bodyStart, bodyEnd)))
                    );
                }
            } else {
                tmpPos++;
            }
        }
    }

    /**
     * Token listesini baştan sona parse ederek programı çalıştırır.
     * Hataları toplar ve ekrana basar.
     */
    public void parseProgram() {
        ilkPassFonksiyonlariKaydet();
        pos = 0;
        while (!isAtEnd()) {
            parseKomut();
        }
        if (errors.isEmpty()) {
            System.out.println("Program geçerli.");
        } else {
            System.out.println("Programda " + errors.size() + " hata bulundu:");
            for (String e : errors) {
                System.err.println(e);
            }
        }
    }

    /**
     * Bir komutu parse eder: değişken tanımı, atama, yazdırma, koşul, döngü, fonksiyon çağrısı, vs.
     */
    private void parseKomut() {
        CBULexer.TokenInfo cur = peek();
        if (cur == null) return;
        switch (cur.token) {
            case Token.FONKSIYON:
                skipFonksiyonTanimi();
                return;
            case Token.TAMSAYI:
            case Token.ONDALIKLI:
            case Token.KELIME:
                parseDegiskenTanimla(cur.token);
                break;
            case Token.DEGISKEN:
                parseAtama();
                break;
            case Token.YAZ:
                parseYazdirma();
                break;
            case Token.EGER:
            case Token.ISE:
                parseEger();
                break;
            case Token.DONGU:
                parseIken();
                break;
            case Token.DON:
                parseDon();
                break;
            default:
                error("Beklenmeyen komut: " + cur.lexeme);
        }
    }

    /** Fonksiyon tanımını (çalışma sırasında) atlar, fonksiyonlar ilk geçişte zaten kaydedilir. */
    private void skipFonksiyonTanimi() {
        advance(); // fonksiyon
        advance(); // ad
        if (match(Token.PARANTEZ_AC)) {
            while (!check(Token.PARANTEZ_KAPA) && !isAtEnd()) advance();
            match(Token.PARANTEZ_KAPA);
        }
        if (match(Token.SURET_AC)) {
            int depth = 1;
            while (depth > 0 && !isAtEnd()) {
                if (check(Token.SURET_AC)) depth++;
                else if (check(Token.SURET_KAPA)) depth--;
                advance();
            }
        }
    }

    /** Değişken tanımlarını parse eder. (tamsayı a = 5; gibi) */
    private void parseDegiskenTanimla(int tipToken) {
        Type veriTipi = null;
        if (tipToken == Token.TAMSAYI) veriTipi = Type.TAMSAYI;
        else if (tipToken == Token.ONDALIKLI) veriTipi = Type.ONDALIKLI;
        else if (tipToken == Token.KELIME) veriTipi = Type.KELIME;

        advance();
        CBULexer.TokenInfo name = peek();
        if (!match(Token.DEGISKEN)) {
            error("Geçerli değişken ismi bekleniyor.");
            return;
        }
        if (symbolTable.containsKey(name.lexeme)) {
            errors.add("Değişken önceden tanımlı: " + name.lexeme);
        } else {
            symbolTable.put(name.lexeme, veriTipi);
        }

        expect(Token.ESITTIR);
        Object val = evaluateIfade();

        if (!isValueCompatible(veriTipi, val)) {
            if (veriTipi == Type.TAMSAYI && val instanceof Double) {
                int newVal = ((Double) val).intValue();
                memory.put(name.lexeme, newVal);
            } else {
                errors.add("Tip uyuşmazlığı: " + name.lexeme);
            }
        } else {
            memory.put(name.lexeme, val);
        }
        expect(Token.NOKTALI_VIRGUL);
    }

    /** Değişken atamalarını işler. (a = 3 + 2; gibi) */
    private void parseAtama() {
        CBULexer.TokenInfo varTok = peek();
        if (!symbolTable.containsKey(varTok.lexeme)) {
            errors.add("Tanımsız değişken: " + varTok.lexeme);
            expect(Token.DEGISKEN);
            expect(Token.ESITTIR);
            evaluateIfade();
            expect(Token.NOKTALI_VIRGUL);
            return;
        }
        expect(Token.DEGISKEN);
        expect(Token.ESITTIR);
        Object val = evaluateIfade();
        Type expected = symbolTable.get(varTok.lexeme);

        if (expected == null) {
            errors.add("Tip bilgisi bulunamıyor: " + varTok.lexeme + " | Semboller: " + symbolTable);
            expect(Token.NOKTALI_VIRGUL);
            return;
        }

        if (!isValueCompatible(expected, val)) {
            if (expected == Type.TAMSAYI && val instanceof Double) {
                int newVal = ((Double) val).intValue();
                memory.put(varTok.lexeme, newVal);
            } else {
                errors.add("Tip uyuşmazlığı: " + varTok.lexeme);
            }
        } else {
            memory.put(varTok.lexeme, val);
        }
        expect(Token.NOKTALI_VIRGUL);
    }

    /** yaz(...) ifadesini işler, ekrana çıktı verir. */
    private void parseYazdirma() {
        expect(Token.YAZ);
        expect(Token.PARANTEZ_AC);
        Object value = evaluateIfade();
        System.out.println(value);
        expect(Token.PARANTEZ_KAPA);
        expect(Token.NOKTALI_VIRGUL);
    }

    /** Eğer/ise bloklarını işler, koşullu çalıştırma yapar. */
    private void parseEger() {
        expect(Token.EGER);
        expect(Token.PARANTEZ_AC);
        boolean cond = evaluateMantiksalIfade();
        expect(Token.PARANTEZ_KAPA);
        expect(Token.ISE);
        expect(Token.SURET_AC);

        if (cond) {
            while (!check(Token.SURET_KAPA) && !isAtEnd()) {
                parseKomut();
            }
        } else {
            while (!check(Token.SURET_KAPA) && !isAtEnd()) {
                advance();
            }
        }
        expect(Token.SURET_KAPA);
    }

    /** döngü (...) { ... } bloklarını işler (while mantığı) */
    private void parseIken() {
        expect(Token.DONGU);
        expect(Token.PARANTEZ_AC);

        int condStart = pos;
        int depth = 1;
        while (depth > 0 && !isAtEnd()) {
            if (check(Token.PARANTEZ_AC))      depth++;
            else if (check(Token.PARANTEZ_KAPA)) depth--;
            if (depth == 0) break;
            advance();
        }
        expect(Token.PARANTEZ_KAPA);

        expect(Token.SURET_AC);
        int bodyStart = pos;
        depth = 1;
        while (depth > 0 && !isAtEnd()) {
            if (check(Token.SURET_AC))      depth++;
            else if (check(Token.SURET_KAPA)) depth--;
            advance();
        }
        int bodyEnd = pos - 1;
        int afterLoop = pos;

        int iter = 0;
        while (true) {
            pos = condStart;
            boolean cond = evaluateMantiksalIfade();
            if (!cond) break;

            pos = bodyStart;
            while (pos < bodyEnd) {
                int before = pos;
                parseKomut();
                if (pos == before) pos++;
            }

            if (++iter >= MAX_LOOP_COUNT) {
                errors.add("Sonsuz döngü şüphesi: " + MAX_LOOP_COUNT + " kez tekrar edildi.");
                break;
            }
        }

        pos = afterLoop;
    }

    /** Fonksiyonlardan dön ifadesini işler, üst fonksiyona dönüş sağlar. */
    private void parseDon() {
        expect(Token.DON);
        Object val = evaluateIfade();
        expect(Token.NOKTALI_VIRGUL);
        throw new ReturnValue(val);
    }

    /** Mantıksal ifadeleri (ve/veya ile birleştirilen) parse eder. */
    private boolean evaluateMantiksalIfade() {
        boolean res = evaluateKosul();
        while (check(Token.VE) || check(Token.VEYA)) {
            int op = advance().token;
            boolean rhs = evaluateKosul();
            res = (op == Token.VE) ? res && rhs : res || rhs;
        }
        return res;
    }

    /** Tek bir koşul ifadesini (a == b vb) parse eder. */
    private boolean evaluateKosul() {
        Object l = evaluateIfade();
        CBULexer.TokenInfo t = peek();
        if (t == null) {
            error("Koşul bekleniyor, ama token yok.");
            return false;
        }
        int op = t.token; advance();
        Object r = evaluateIfade();
        if (l instanceof Number ln && r instanceof Number rn) {
            double a = ln.doubleValue(), b = rn.doubleValue();
            switch (op) {
                case Token.ESIT_ESIT: return a == b;
                case Token.ESIT_DEGIL: return a != b;
                case Token.KUCUK: return a < b;
                case Token.KUCUK_ESIT: return a <= b;
                case Token.BUYUK: return a > b;
                case Token.BUYUK_ESIT: return a >= b;
                default: return false;
            }
        }
        return false;
    }

    /**
     * Matematiksel ifadeleri parse ve evaluate eder. (Örn: 2 + 3 * 5)
     * Not: Burada işlem önceliği desteği için daha gelişmiş ayrıştırıcı gerekir.
     */
    private Object evaluateIfade() {
        CBULexer.TokenInfo t = peek();
        Object res;

        // (ifade) desteği eklendi
        if (match(Token.PARANTEZ_AC)) {
            res = evaluateIfade();
            expect(Token.PARANTEZ_KAPA);
        }
        else if (match(Token.SAYI)) {
            if (t.lexeme.contains(".")) {
                try {
                    res = Double.parseDouble(t.lexeme);
                } catch (NumberFormatException e) {
                    error("Geçersiz ondalıklı sayı: " + t.lexeme);
                    res = 0;
                }
            } else {
                try {
                    res = Integer.parseInt(t.lexeme);
                } catch (NumberFormatException e) {
                    error("Geçersiz tamsayı: " + t.lexeme);
                    res = 0;
                }
            }
        } else if (match(Token.STRING)) {
            res = t.lexeme;
        }
        else if (check(Token.DEGISKEN)) {
            // Eğer bir sonraki token PARANTEZ_AC ise fonksiyon çağrısıdır!
            if (pos+1 < tokens.size() && tokens.get(pos+1).token == Token.PARANTEZ_AC) {
                if (fonksiyonTablosu.containsKey(t.lexeme)) return evaluateFonksiyonCagri();
                return evaluateYerlesikFonksiyon();
            } else {
                match(Token.DEGISKEN);
                res = memory.getOrDefault(t.lexeme, 0);
            }
        }
        else {
            error("İfade bekleniyor, gelen: " + (t != null ? t.lexeme : "EOF"));
            return 0;
        }
        while (match(Token.TOPLA) || match(Token.CIKAR) ||
                match(Token.CARP)  || match(Token.BOL)   ||
                match(Token.MOD)) {
            int op = tokens.get(pos-1).token;
            Object right = evaluateIfade();
            if (res instanceof Number ln && right instanceof Number rn) {
                double a = ln.doubleValue(), b = rn.doubleValue();
                switch (op) {
                    case Token.TOPLA: res = a + b; break;
                    case Token.CIKAR: res = a - b; break;
                    case Token.CARP:  res = a * b; break;
                    case Token.BOL:   res = b != 0 ? a/b : 0; break;
                    case Token.MOD:   res = a % b; break;
                    default:          res = 0;
                }
                if (op != Token.BOL && op != Token.MOD && ((double) res) % 1 == 0) {
                    res = (int) Math.round((double) res);
                }
            }
        }
        return res;
    }

    /** Yerleşik fonksiyon çağrılarını işler (uzunluk, karesi, tarih, oku). */
    private Object evaluateYerlesikFonksiyon() {
        CBULexer.TokenInfo fn = peek(); advance();
        expect(Token.PARANTEZ_AC);
        Object arg = check(Token.PARANTEZ_KAPA) ? null : evaluateIfade();
        expect(Token.PARANTEZ_KAPA);
        switch (fn.lexeme) {
            case "uzunluk": return (arg instanceof String s) ? s.length() : 0;
            case "karesi":  return (arg instanceof Number n) ? Math.pow(n.doubleValue(),2) : 0;
            case "tarih":   return LocalDate.now().toString();
            case "oku":
                if (arg instanceof String filename) {
                    try {
                        return Files.readString(Path.of(filename));
                    } catch (Exception e) {
                        errors.add("Dosya okunamadı: " + filename);
                        return "";
                    }
                }
                return "";
            default:
                errors.add("Bilinmeyen fonksiyon: " + fn.lexeme);
                return 0;
        }
    }

    /** Kullanıcı tanımlı fonksiyon çağrısını işler. */
    private Object evaluateFonksiyonCagri() {
        CBULexer.TokenInfo fn = peek(); advance();
        expect(Token.PARANTEZ_AC);
        List<Object> args = new ArrayList<>();
        if (!check(Token.PARANTEZ_KAPA)) {
            do args.add(evaluateIfade());
            while (match(Token.VIRGUL));
        }
        expect(Token.PARANTEZ_KAPA);

        Function f = fonksiyonTablosu.get(fn.lexeme);
        if (f==null || f.parametreler.size()!=args.size()) {
            errors.add("Fonksiyon hatası/argüman sayısı: " + fn.lexeme);
            return 0;
        }
        Map<String,Object> backup = new HashMap<>(memory);
        for (int i = 0; i < args.size(); i++) {
            memory.put(f.parametreler.get(i), args.get(i));
        }
        CBUParser sub = new CBUParser(f.govde);
        sub.symbolTable.putAll(symbolTable);
        sub.memory.putAll(memory);
        sub.fonksiyonTablosu.putAll(fonksiyonTablosu);
        try {
            sub.parseProgram();
        } catch (ReturnValue r) {
            memory.putAll(sub.memory);
            errors.addAll(sub.errors);
            return r.value;
        }
        memory.putAll(sub.memory);
        errors.addAll(sub.errors);
        return 0;
    }

    /** İfade ve tip uyuşmazlık kontrolü. */
    private boolean isValueCompatible(Type t, Object v) {
        if (t == null || v == null) return false;
        switch (t) {
            case TAMSAYI:
                return v instanceof Integer || (v instanceof Double && ((Double) v) % 1 == 0);
            case ONDALIKLI:
                return v instanceof Double || v instanceof Integer;
            case KELIME:
                return v instanceof String;
        }
        return false;
    }

    /** Token'ı ileri sarma ve yardımcı fonksiyonlar */
    private CBULexer.TokenInfo peek() {
        return isAtEnd() ? null : tokens.get(pos);
    }
    private CBULexer.TokenInfo advance() {
        return tokens.get(pos++);
    }
    private boolean match(int exp) {
        if (check(exp)) {
            pos++;
            return true;
        }
        return false;
    }
    private boolean check(int exp) {
        return !isAtEnd() && peek().token == exp;
    }
    private void expect(int exp) {
        if (!match(exp)) {
            CBULexer.TokenInfo t = peek();
            String loc = (t != null) ? " [" + t.line + "," + t.column + "]" : "";
            errors.add("Beklenen " + exp + ", gelen: "
                    + (t != null ? t.token + "(" + t.lexeme + ")" : "EOF")
                    + loc);
        }
    }
    private void error(String msg) {
        CBULexer.TokenInfo t = peek();
        String loc = (t != null) ? " [" + t.line + "," + t.column + "]" : "";
        errors.add(msg + loc);
    }
    private boolean isAtEnd() {
        return pos >= tokens.size();
    }
}
