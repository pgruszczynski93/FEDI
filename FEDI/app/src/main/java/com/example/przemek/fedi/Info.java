package com.example.przemek.fedi;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Info extends AppCompatActivity {


    DisplayMetrics _dm;
    int _dispW, _dispH;
    TextView _imageInfoText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menuinfo);

        getSupportActionBar().setTitle("O aplikacji");

        _imageInfoText = (TextView)findViewById(R.id.menuInfoText);
        _imageInfoText.setMovementMethod(new ScrollingMovementMethod());

        InitDialog();
        FillTextView();
    }
//
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
//        startActivity(new Intent(Info.this, MainMenu.class));
//    }

    public void ShowMainMenu(View view){
        finish();
    }

    void InitDialog(){
        _dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(_dm);

        _dispW = _dm.widthPixels;
        _dispH = _dm.heightPixels;

        getWindow().setLayout((int)(_dispW * .9), (int)(_dispH*.9));
    }


    @SuppressWarnings("deprecation")
    void FillTextView(){
        String myAttribute = "";
        myAttribute += ("<b>Nazwa aplikacji:</b><br>FEDI<br><br>");
        myAttribute += ("<b>Wersja:</b><br>1.0<br><br>");
        myAttribute += ("<b>Autor:</b><br>Przemysław Gruszczyński<br><br>");
        myAttribute += ("<b>Kierunek:</b><br>Informatyka Stosowana<br><br>");
        myAttribute += ("<b>============ Instrukcja ============ </b><br><br>");
        myAttribute += ("Menu Główne dostarcza następujące opcje:<br>1. Galeria - Wczytanie obrazu z pamięci telefonu.<br>" +
                "2. Aparat - Zrobienie nowego zdjęcia za pomocą wybranego aparatu zainstalowanego w telefonie.<br>" +
                "3. Informacje - Wyświetlenie informacj na temat aplikacji. <br><br>" +
                "Wybór opcji 1 lub 2 przeniesie Cię do okna edytora, umożliwiającego modyfikację zdjęć. Opcja numer 3 wyświetla widoczne okno.<br><br>");
        myAttribute += ("<b>Edytor:</b><br>1. Górny pasek Edytora udostępnia następujące opcje: <br>" +
                "- Otwórz - otwiera nowe zdjęcie do edycji.<br>" +
                "- Podgląd - wyświetla oryginalne zdjęcie.<br>" +
                "- Informacje - wyświetla informacje o zdjęciu. <br>" +
                "- Reset skali - skaluje przybliżone zdjęcie do oryginalnie wyświetlanych rozmiarów.<br>" +
                "- Zapis - zapisuje zmodyfikowane zdjęcie.<br>"+
                "- Pokaż/Ukryj - pokazuje/ukrywa elementy Edytora.<br><br>");
        myAttribute += ("2. Okno widoku:</b><br>Za pomocą gestów szczypania oraz przesuwania, możesz odpowiednio przybliżać oraz przesuwać " +
                "zdjęcie w obrębie widoku.<br><br>");
        myAttribute += ("3. Opcje edycji: </b><br>Dolny pasek Edytora udostępnia następujące opcje: <br>" +
                "- Dopasuj<br>" +
                "- Histogram<br>" +
                "- Balans Bieli<br>" +
                "- Szczegóły<br>" +
                "- Rozmycie<br>" +
                "- Filtry: Szum<br>" +
                "- Filtry: Ogólne<br>" +
                "- Filtry: Natura<br>" +
                "- Filtry: Szarości<br>" +
                "- Obróć. <br><br>");
        myAttribute += ("4. Wyjście z edytora:</b><br>Aby wyjść z Edytora należy dwukrotnie wcisnąć przycisk WSTECZ.");

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            _imageInfoText.setText(Html.fromHtml(myAttribute,Html.FROM_HTML_MODE_COMPACT));
        }
        else {
            _imageInfoText.setText(Html.fromHtml(myAttribute));
        }
    }
}
