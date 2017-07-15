#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float kelvin_value;

/// dorobić bledndowanie z rgb albo wywalic
uchar4 __attribute__((kernel)) kelvin_temperature(uchar4 pixel_in, uint32_t x, uint32_t y){


    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 rgb_pixel = full_pixel.rgb;

    kelvin_value = ClampFloat(20000, 1000.0f, 40000.0f) / 100.0f;

    if(kelvin_value <= 66.0f){
        rgb_pixel.r = 1.0f;
        rgb_pixel.g = (99.470 * log(kelvin_value) - 161.119)/255.0f;
    }
    else{
        rgb_pixel.r = (329.698 * pow(kelvin_value - 60, -0.133))/255.0f;
        rgb_pixel.g = (288.122 * pow(kelvin_value - 60, -0.075))/255.0f;
    }

    if(kelvin_value >= 66){
        rgb_pixel.b = 1.0f;
    }
    else if(kelvin_value <= 19){
        rgb_pixel.b = 0.0f;
    }
    else{
        rgb_pixel.b = (138.517 * log(kelvin_value - 10) - 305.044)/255.0f;
    }

    rgb_pixel = Clamp01Float3(rgb_pixel);
    return rsPackColorTo8888(rgb_pixel);

}