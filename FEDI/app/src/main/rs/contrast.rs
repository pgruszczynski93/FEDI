#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed


float contrast_value;
// liczony tylko dla wspolczynnika kontrastu (bez jasnosci)

static float get_contrast_factor(){
     return (1.0f + contrast_value) * (1.0f + contrast_value);;
}
// wzor: Truncate(factor * (Red(colour)   - 128) + 128) truncate - klampowanie

uchar4 __attribute__((kernel)) contrast(uchar4 pixel_in, uint32_t x, uint32_t y){
    uchar4 pixel_out;
    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb_pix = full_pixel.rgb;

    if(get_contrast_factor() != 1){
        rgb_pix -= 0.5f;
        rgb_pix *= get_contrast_factor();
        rgb_pix += 0.5f;
        rgb_pix = Clamp01Float3(rgb_pix);
    }

    pixel_out = rsPackColorTo8888(rgb_pix);
    return pixel_out;
}