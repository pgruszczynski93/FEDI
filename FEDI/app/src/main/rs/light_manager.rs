#include "utils.rsh"

#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

//wartość pobrana z suwaka odpowiedzialna za przyciemnienie/rozswietlenie
float light_value;
//tryb pracy kernela: rozjasnianie/przyciemnianie obszarow ciemnych
bool light_mode;

// kernrel odpowiedzialny za generowanie prześwietleń/przyciemnień
uchar4 __attribute__((kernel)) light_add_sub(uchar4 pixel_in, uint32_t x, uint32_t y){
    float4 full_pixel = rsUnpackColor8888(pixel_in);
    float3 multiplied_pixel = full_pixel.rgb;
    if(dot(full_pixel.rgb, yuv_vector) >= 0.6 && light_mode){
        multiplied_pixel += light_value;
    }
    else if(dot(full_pixel.rgb, yuv_vector) <= 0.6 && !light_mode){
        multiplied_pixel += light_value;
    }
    multiplied_pixel = Clamp01Float3(multiplied_pixel);
    return rsPackColorTo8888(multiplied_pixel);
}