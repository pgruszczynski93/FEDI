#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

rs_allocation img_in;
uint32_t width, height;
float size, ratio;
uint32_t x_center, y_center, vig_max, vig_min, diff;


uchar4 __attribute__((kernel)) vignette_filter(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pix = rsUnpackColor8888(pixel_in);
    int32_t distance_x, distance_y, distance_euclides;
    float3 rgb;
    float vignette;
    ratio = (width > height) ? ((float) height / width) : ((float) width / height);
    x_center = width >> 1;
    y_center = height >> 1;
    vig_max = x_center*x_center + y_center * y_center;
    vig_min = vig_max * (1-size)* (1-size);
    diff = vig_max - vig_min;
    distance_x = x_center - x;
    distance_y = y_center - y;

    if(width > height){
        distance_x *=  ratio;
    }
    else{
        distance_y *=  ratio;
    }

    distance_euclides = distance_x*distance_x + distance_y*distance_y;

    if(distance_euclides > vig_min){
        vignette = (float)(vig_max - distance_euclides) / (float)diff;
        vignette *= vignette ;
        rgb = full_pix.rgb * vignette;
        rgb = Clamp01Float3(rgb);
    }
    else{
        rgb = full_pix.rgb;
    }
    return rsPackColorTo8888(rgb);
}

