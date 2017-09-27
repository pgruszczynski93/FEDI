#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

int32_t y_histogram[256]; // tablica liczności miar jasności piksela
float y_equalized_histogram[256]; //tablica liczności miar piksela po operacji wyrównania
int size; // rozmiar obrazu


// kernel odpowiedzialny za konwersje piksela z rgb do yuv i stworzenie histogramu luminancji
uchar4 __attribute__((kernel)) equalize_histogram_rgbyuv(uchar4 in, uint32_t x, uint32_t y) {
    float4 f4 = rsUnpackColor8888(in);
    float3 yuv;
    yuv = RGBtoYUV(f4.rgb);
    int32_t hist_index = yuv.x * 255;
    rsAtomicInc(&y_histogram[hist_index]); // zwiększenie wartosci i-tego elementu histogramu; rsAtomicInc - inkrementacja bezpieczna nie powodująca konfilktu wątkow
    return rsPackColorTo8888(yuv);
}
// kernel odpowiedzialny za konwersje z przestrzeni yuv do rgb
uchar4 __attribute__((kernel)) equalize_histogram_yuvrgb(uchar4 in, uint32_t x, uint32_t y) {
    float4 f4 = rsUnpackColor8888(in);
    float3 rgb, yuv;
    int32_t hist_index;
    yuv.x = f4.r;
    hist_index = yuv.x * 255;
    yuv.x = y_equalized_histogram[hist_index];
    // na podstawie wzoru z utils.h
    yuv.y = (2*f4.g)-1;
    yuv.z = (2*f4.b)-1;
    rgb = Clamp01Float3(YUVtoRGB(yuv));
    return rsPackColorTo8888(rgb);
}
// inicjalizacja zmiennych i tablic
void setup() {
    for (int i = 0; i < MAX_CHANNEL_VALUE; i++) {
        y_histogram[i] = 0;
        y_equalized_histogram[i] = 0.0f;
    }
}
// wyrónanie histogramu miar jasności; przemapowanie skladowej y
void equalize_y_histogram() {
    float sum = 0;
    for (int i = 0; i < MAX_CHANNEL_VALUE; i++) {
        sum += y_histogram[i];
        y_equalized_histogram[i] = sum / (size);
    }
}