#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float brightTreshold;   // wspolczynnik jasności

// kernel przekształcający piksel pod efekt Bloom
uchar4 __attribute__((kernel)) bloom_bright_pass(uchar4 pixel_in){
    float3 luminance_vec = {0.2125, 0.7154, 0.0721 };
    float4 pixel = rsUnpackColor8888(pixel_in);
    float local_luminance = dot(luminance_vec, pixel.rgb);  // obliczenie luminancji piksela

    local_luminance = max(0.0f, local_luminance - brightTreshold);  //wybranie większej z pary 0.0, róóżnica luminancji i progu jasności
    pixel.rgb *= sign(local_luminance); //okreslenie znaku kanałów na postawie luminancji
    pixel.a = 1.0;

    return rsPackColorTo8888(clamp(pixel, 0.f, 1.f));
}