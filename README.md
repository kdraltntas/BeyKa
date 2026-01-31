# BeyKa Programming Language 🚀

**BeyKa**, Java tabanlı geliştirilmiş, Türkçe sözdizimine (syntax) sahip, eğitim amaçlı bir programlama dili yorumlayıcısıdır (Interpreter).

Bu proje, **Bilgisayar Mühendisliği** prensipleri çerçevesinde; biçimsel diller (formal languages), derleyici tasarımı (compiler design) ve bellek yönetimi (memory management) kavramlarını pratikte uygulamak amacıyla geliştirilmiştir.

---

## 🏗 Mimari ve Teknik Detaylar (Architecture)

BeyKa, kaynak kodu çalıştırmak için klasik bir derleyici ön yüzü (compiler front-end) mimarisi kullanır:

### 1. Sözcük Analizi (Lexical Analysis)
* **Lexer (`BeyKaLexer.java`):** Kaynak kod karakter akışı olarak okunur ve anlamlı parçalara (token) ayrılır.
* **Özellikler:** Türkçe karakter desteği, String literal işleme, Yorum satırları (`//`) ve Operatör önceliklendirme.

### 2. Sözdizimi Analizi ve Yorumlama (Parsing & Interpreting)
* **Recursive Descent Parser (`BeyKaParser.java`):** Token listesi, özyinelemeli iniş ayrıştırıcısı ile işlenir. Bu yöntem, dilin gramer kurallarını (Context-Free Grammar) kod yapısında doğrudan yansıtır.
* **Scope Yönetimi:** Fonksiyon çağrılarında (`evaluateFonksiyonCagri`), her çağrı için yeni bir Parser instance'ı oluşturularak **Call Stack** (Çağrı Yığını) simüle edilir. Bu sayede yerel (local) ve global değişkenler birbirine karışmaz.
* **Tip Güvenliği (Type Safety):** Değişken atamalarında (`tamsayı`, `ondalikli`, `kelime`) statik tip kontrolü yapılır.

### 3. Loglama ve I/O
* Sistem; token listesini (`tokens.txt`), program çıktısını (`output.txt`) ve hataları (`errors.txt`) ayrı dosyalara loglar. Konsol çıktısı `TeeOutputStream` ile hem ekrana hem dosyaya eşzamanlı basılır.

---

## 💻 Sözdizimi Örnekleri (Syntax Examples)

BeyKa, okunabilirliği yüksek, C-benzeri blok yapısına sahip Türkçe bir sözdizimi kullanır.

### Değişken Tanımlama ve Aritmetik
```
tamsayı a = 10;
tamsayı b = 20;
tamsayı sonuc = (a + b) * 2;

yaz(sonuc); // Çıktı: 60
```

### Koşullu İfadeler (If-Else)
```
eğer (sonuc > 50) ise {
    yaz("Sonuç 50'den büyük");
}
```




### Döngüler (Loops)
```
tamsayı i = 0;
döngü (i < 5) {
    yaz(i);
    i = i + 1;
}
```


### Fonksiyon Tanımlama (Functions)
```
fonksiyon topla(x, y) {
    dön x + y;
}

tamsayı toplam = topla(5, 10);
yaz(toplam);
```


### 🛠 Kurulum ve Çalıştırma (Installation & Usage)
Projeyi yerel ortamınızda çalıştırmak için JDK (Java Development Kit) gereklidir.

Repoyu Klonlayın:
git clone https://github.com/kdraltntas/BeyKa.git

Derleyin:
javac *.java

Örnek Bir Kod Çalıştırın: Kendi .bka uzantılı dosyanızı oluşturun veya örnekleri kullanın.

java BeyKa ornekler/test.bka

### 👨‍💻 Geliştirici
Recep Kadir Altıntaş