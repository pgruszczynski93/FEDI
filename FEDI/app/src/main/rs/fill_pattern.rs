#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in; // alokacja wejściowa
uint32_t width, height; // wysokość i szerokość
float intensity;    // wspolczynnik intensywności
float mix_1, mix_2;  // zmienne odpowiedzialne za

// kernel odpowiedzialny za przeksztalcenie piksela według filtra wypełnienie światłem
uchar4 __attribute__((kernel)) fill_pattern(uchar4 pixel_in, uint32_t x, uint32_t y){
	//uint32_t new_x, new_y;
    float4 full_pix = rsUnpackColor8888(pixel_in), new_fullpix;
    float3 rgb, new_rgb;
    mix_1 = intensity;
	mix_2 = 1.0f - intensity;
    rgb = Clamp01Float3(2*full_pix.rgb);
    new_rgb = full_pix.rgb* mix_2 + rgb * mix_1;
    return rsPackColorTo8888(new_rgb);
}
