#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;   // alokacja wejsciowa
uint32_t width, height; // szerokosc, wyskossc
float size, ratio;      // rozmiar zdjecia, wspolczynnik
uint32_t x_center, y_center, vig_max, vig_min, diff; // wspolrzedne srodka; maksymalna wartosc winiety, minimalna wartośc winiety, różnica


// kernel odpowiedzialny za przekształcenie piksela do winiety
uchar4 __attribute__((kernel)) vignette_filter(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    int32_t distance_x, distance_y, distance_euclides;
    float3 rgb;
    float vignette;
    ratio = (width > height) ? ((float) height / width) : ((float) width / height); // sprawdzenie proporcji zdjęcia
    x_center = width >> 1;          // ustalenie środka winiety, wsp x
    y_center = height >> 1;         // ustalenie środka winiety, wsp y
    vig_max = x_center*x_center + y_center * y_center;  //maksimum winiety jako suma kwaadratów
    vig_min = vig_max * (1-size)* (1-size);     //minimum winiety
    diff = vig_max - vig_min;                   //roznica max i min
    distance_x = x_center - x;                  //odbliczenie odleglosci wsp x aktualnego piksela od srodka
    distance_y = y_center - y;                  //odbliczenie odleglosci wsp y aktualnego piksela od srodka

// w zaleznosci od proporcji ekranu nastpeuje wybranie kuerunku, który zostanei pomnozony przez wspolczynnik proporcji
    if(width > height){
        distance_x *=  ratio;
    }
    else{
        distance_y *=  ratio;
    }

//obliczenie odległosci po prostej
    distance_euclides = distance_x*distance_x + distance_y*distance_y;

//jesli oobliczona odleglosc euklidesowa jest wieksza od minimalnej to nastepuje przekształenei piksela wg wag; w przeciwnym wypadku piksel oryginalny
    if(distance_euclides > vig_min){
    // obliczenie winiety
        vignette = (float)(vig_max - distance_euclides) / (float)diff;
        // pomnozenie piksela przez wartosc winiety
        vignette *= vignette ;
        rgb = full_pix.rgb * vignette;
        // sprawdzenie granic
        rgb = Clamp01Float3(rgb);
    }
    else{
        rgb = full_pix.rgb;
    }
    return rsPackColorTo8888(rgb);
}

