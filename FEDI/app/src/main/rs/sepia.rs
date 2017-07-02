#pragma version(1)
#pragma rs java_package_name(com.example.przemek.fedi)

const float3 sepia_red = {0.393f, 0.769f, 0.189f};
const float3 sepia_green = {0.349f, 0.686, 0.168f};
const float3 sepia_blue = {0.272f, 0.534f, 0.131f};

uchar4 __attribute__((kernel)) sepia(uchar4 pixel_in, uint32_t x, uint32_t y){

    uchar4 pixel_out;
    float4 pix_to_float = rsUnpackColor8888(pixel_in);
    float3 out;
    out.r = dot(pix_to_float.rgb, sepia_red);
    out.g = dot(pix_to_float.rgb, sepia_green);
    out.b = dot(pix_to_float.rgb, sepia_blue);

    out.r = (out.r > 1) ? 1 : out.r;
    out.g = (out.g > 1) ? 1 : out.g;
    out.b = (out.b > 1) ? 1 : out.b;
    pixel_out = rsPackColorTo8888(out);

    return pixel_out;
}

