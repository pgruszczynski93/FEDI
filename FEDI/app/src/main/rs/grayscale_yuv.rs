#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)

const float3 yuv_vector= {0.299f, 0.587f, 0.114f};

uchar4 __attribute__((kernel)) grayscale_yuv(uchar4 pixel_in, uint32_t x, uint32_t y){
    uchar4 pixel_out;
    float4 unpacked_pixel = rsUnpackColor8888(pixel_in);
    float3 multiplied_pixel = dot(unpacked_pixel.rgb, yuv_vector);
    pixel_out = rsPackColorTo8888(multiplied_pixel);
    return pixel_out;
}