#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float contrast_value;
// liczony tylko dla wspolczynnika kontrastu (bez jasnosci)

// obliczenie współczynnika kontrastu
static float get_contrast_factor(){
     return (1.0f + contrast_value) * (1.0f + contrast_value);;
}

// kernel zmiany kontrastu piksela
uchar4 __attribute__((kernel)) contrast(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb_pix = full_pixel.rgb;

    if(get_contrast_factor() != 1){
        rgb_pix -= 0.5f;
        rgb_pix *= get_contrast_factor();
        rgb_pix += 0.5f;
        rgb_pix = Clamp01Float3(rgb_pix);
    }

    return rsPackColorTo8888(rgb_pix);
}