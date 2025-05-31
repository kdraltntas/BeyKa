/**
 * Token sınıfı: CBU dilinin lexer ve parser'ında kullanılan bütün token türlerinin sabit değerlerini içerir.
 * Her token bir tamsayı ile temsil edilir. Bu sabitler, kaynak kodun analizinde kullanılır.
 */
public class Token {
    /** Veri tiplerini tanımlayan tokenlar */
    public static final int TAMSAYI         = 32;   // tamsayı anahtar kelimesi
    public static final int ONDALIKLI       = 33;   // ondalıklı anahtar kelimesi
    public static final int KELIME          = 34;   // kelime anahtar kelimesi

    /** Karşılaştırma ve atama operatörleri */
    public static final int ESITTIR         = 57;   // =
    public static final int ESIT_ESIT       = 100;  // ==
    public static final int ESIT_DEGIL      = 101;  // !=
    public static final int KUCUK_ESIT      = 102;  // <=
    public static final int KUCUK           = 104;  // <
    public static final int BUYUK_ESIT      = 105;  // >=
    public static final int BUYUK           = 106;  // >

    /** Mantıksal operatörler */
    public static final int VE              = 301;  // &&
    public static final int VEYA            = 302;  // ||

    /** Aritmetik operatörler */
    public static final int TOPLA           = 40;   // +
    public static final int CIKAR           = 41;   // -
    public static final int CARP            = 42;   // *
    public static final int BOL             = 43;   // /
    public static final int MOD             = 46;   // %
    public static final int ARTTIR          = 44;   // ++
    public static final int AZALT           = 45;   // --

    /** Parantez ve blok başlangıcı/bitişi tokenları */
    public static final int PARANTEZ_AC     = 80;   // (
    public static final int PARANTEZ_KAPA   = 81;   // )
    public static final int SURET_AC        = 200;  // {
    public static final int SURET_KAPA      = 201;  // }

    /** Noktalama işaretleri */
    public static final int NOKTALI_VIRGUL  = 60;   // ;
    public static final int VIRGUL          = 82;   // ,
    public static final int NOKTA           = 108;  // .

    /** Anahtar kelimeler */
    public static final int YAZ             = 70;   // yaz komutu
    public static final int ISE             = 109;  // ise
    public static final int DEGILSE         = 111;  // değilse
    public static final int DONGU           = 112;  // döngü
    public static final int DURDUR          = 113;  // durdur
    public static final int DEVAM           = 114;  // devam
    public static final int EGER            = 110;  // eğer
    public static final int FONKSIYON       = 115;  // fonksiyon
    public static final int DON             = 120;  // dön

    /** Değişken ve sabit türleri için tokenlar */
    public static final int DEGISKEN        = 21;   // değişken isimleri
    public static final int SAYI            = 15;   // sayısal sabitler (literal)
    public static final int STRING          = 300;  // string sabitler

    /** Diğer (tanımsız, hata, vb.) */
    public static final int DIGER           = -1;   // Diğer, tanımsız, hatalı karakterler için
}
