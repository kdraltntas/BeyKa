public class Token {
    // Veri tipleri
    public static final int TAMSAYI         = 32;
    public static final int ONDALIKLI       = 33;
    public static final int KELIME          = 34;

    // Operatörler ve karşılaştırmalar
    public static final int ESITTIR         = 57;   // =
    public static final int ESIT_ESIT       = 100;  // ==
    public static final int ESIT_DEGIL      = 101;  // !=
    public static final int KUCUK_ESIT      = 102;  // <=
    public static final int KUCUK           = 104;  // <
    public static final int BUYUK_ESIT      = 105;  // >=
    public static final int BUYUK           = 106;  // >

    // Mantıksal operatörler
    public static final int VE              = 301;  // &&
    public static final int VEYA            = 302;  // ||

    // Aritmetik operatörler
    public static final int TOPLA           = 40;   // +
    public static final int CIKAR           = 41;   // -
    public static final int CARP            = 42;   // *
    public static final int BOL             = 43;   // /
    public static final int MOD             = 46;   // %
    public static final int ARTTIR          = 44;   // ++
    public static final int AZALT           = 45;   // --

    // Parantez ve blok sembolleri
    public static final int PARANTEZ_AC     = 80;   // (
    public static final int PARANTEZ_KAPA   = 81;   // )
    public static final int SURET_AC        = 200;  // {
    public static final int SURET_KAPA      = 201;  // }

    // Noktalama
    public static final int NOKTALI_VIRGUL  = 60;   // ;
    public static final int VIRGUL          = 82;   // ,
    public static final int NOKTA           = 108;  // .

    // Anahtar kelimeler
    public static final int YAZ             = 70;
    public static final int ISE             = 109;
    public static final int DEGILSE         = 111;
    public static final int DONGU           = 112;
    public static final int DURDUR          = 113;
    public static final int DEVAM           = 114;
    public static final int EGER            = 110;
    public static final int FONKSIYON       = 115; // fonksiyon
    public static final int DON             = 120; // dön

    // Değişken ve sabit türleri
    public static final int DEGISKEN        = 21;
    public static final int SAYI            = 15;
    public static final int STRING          = 300;

    // Diğer
    public static final int DIGER           = -1;
}
