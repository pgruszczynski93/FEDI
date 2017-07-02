#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)
#pragma rs_fp_relaxed

float treshold_value;
const float3 yuv_vector= {0.299f, 0.587f, 0.114f};

uchar4 __attribute__((kernel)) treshold(uchar4 pixel_in, uint32_t x, uint32_t y){
    uchar4 pixel_out;
    float4 unpacked_pixel = rsUnpackColor8888(pixel_in);
    float grayscale_value = dot(unpacked_pixel.rgb, yuv_vector);
    grayscale_value = (grayscale_value > treshold_value) ? 1 : 0;
    float3 out = {grayscale_value,grayscale_value,grayscale_value};
    pixel_out = rsPackColorTo8888(out);
    return pixel_out;
}
