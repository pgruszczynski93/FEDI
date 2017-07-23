#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t width, height;
float intensity;
float mix_1, mix_2;


uchar4 __attribute__((kernel)) fill_pattern(uchar4 pixel_in, uint32_t x, uint32_t y){
	uint32_t new_x, new_y;
    float4 full_pix = rsUnpackColor8888(pixel_in), new_fullpix;
    float3 rgb, new_rgb;
    mix_1 = intensity;
	mix_2 = 1.0f - intensity;
	new_x = x%width;
	new_y = y%height;
	new_fullpix = rsUnpackColor8888(*(const uchar4*)rsGetElementAt(img_in, new_x, new_y));
    rgb = Clamp01Float3(full_pix.rgb + new_fullpix.rgb);
    new_rgb = full_pix.rgb* mix_2 + rgb * mix_1;
    return rsPackColorTo8888(new_rgb);
}
