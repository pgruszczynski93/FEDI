#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)

uchar4 __attribute__((kernel)) grayscale_yuv(uchar4 pixel_in, uint32_t x, uint32_t y){
    uchar4 pixel_out;
    uchar yuv_sum = pixel_in.r*0.299f+ pixel_in.g*0.587f + pixel_in.b*0.114f;
    pixel_out.r = pixel_out.g = pixel_out.b = yuv_sum;
    return pixel_out;
}