package com.chromiumikx.playerbyjava;

import com.chromiumikx.instream.IRandomAccess;
import com.sun.scenario.effect.InvertMask;

public final class MP3Header {
	public static final int MPEG1 = 3;
	public static final int MPEG2 = 2;
	public static final int MPEG25 = 2;
	public static final int MAX_FRAMESIZE = 1732;

    /* 
     * intBitrateTable[intLSF][intLayer-1][intBitrateIndex] 
     */
    private static final int[][][] intBitrateTable = {
            {  
                //MPEG 1  
                //Layer I  
                {0,32,64,96,128,160,192,224,256,288,320,352,384,416,448},  
                //Layer II  
                {0,32,48,56, 64, 80, 96,112,128,160,192,224,256,320,384},  
                //Layer III  
                {0,32,40,48, 56, 64, 80, 96,112,128,160,192,224,256,320}  
            },  
            {  
                //MPEG 2.0/2.5  
                //Layer I  
                {0,32,48,56,64,80,96,112,128,144,160,176,192,224,256},  
                //Layer II  
                {0,8,16,24,32,40,48,56,64,80,96,112,128,144,160},  
                //Layer III  
                {0,8,16,24,32,40,48,56,64,80,96,112,128,144,160}  
            }
    };
   
    /* 
     * intSamplingRateTable[intVersionID][intSamplingFrequency] 
     */
    private static final int[][] intSamplingRateTable = {  
            {11025 , 12000 , 8000,0},   //MPEG Version 2.5  
            {0,0,0,0,},                 //reserved  
            {22050, 24000, 16000 ,0},   //MPEG Version 2 (ISO/IEC 13818-3)  
            {44100, 48000, 32000,0}     //MPEG Version 1 (ISO/IEC 11172-3)  
    };
	
	private static int intVersionID;//版本，2位，00-MPEG2.5，01-未定义，10-MPEG2，11-MPEG1
	private static int intLayer;//层，MPEG共分为3层，第三层解码最复杂，2位，00-未定义，01-Layer3，10-Layer2，11-Layer1
	private static int intProtectionBit;//CRC校验，1位，0-校验，1-不校验
	private static int intBitrateIndex;//主数据的位率
	private static int intSamplingFreq;//PCM采样率
	private static int intPadding;//字节填充位，置1即为有1字节填充
	private static int intPrivate;
	private static int intMode;//声道模式：0-立体声；1-联合立体声；2-双声道；3-单声道
	private static int intModeExtension;//表示采用哪种联合立体声方式
	private static int intCopyright;//版权
	private static int intOriginal;//是否是原版数据
	private static int intEmphasis;//预加重
	
	private static int intFrameSize;
	private static int intMainDataBytes;//主数据长度
	private static int intSideInfoSize;//边信息长度
	private static int intLSF;
	private static int intStandardMask = 0xffe00000;
	private static boolean boolMS_Stereo,boolIntensity_Stereo;
	private static IRandomAccess iraInput;
	
	/*
	 * 构造函数
	 */
	public MP3Header(IRandomAccess in_rai){
		iraInput = in_rai;
	}
	
	
	/*
	 * 帧头解析
	 */
	private void parseHeader(int h){
		intVersionID = (h >> 19) & 3;
		intLayer = 4-(h >> 17) & 3;
		intProtectionBit = (h >> 16) & 0x1;
		intBitrateIndex = (h >> 12) & 0xF;
		intSamplingFreq = (h >> 10) & 3;
		intPadding = (h >> 9) & 0x1;
		intMode = (h >> 6) & 3;
		intModeExtension = (h >> 4) & 3;
		
		boolMS_Stereo = intMode == 1 && (intModeExtension & 2) != 0;
		boolIntensity_Stereo = intMode == 1 && (intModeExtension & 0x1)!= 0;
		intLSF = (intVersionID == MPEG1) ? 0 : 1;
		
		/*
		 * 由表翻译Layer相关的信息
		 */
		switch (intLayer) {
		case 1:
			intFrameSize = intBitrateTable[intLSF][0][intBitrateIndex]*12000;
			intFrameSize /= intSamplingRateTable[intVersionID][intSamplingFreq];
			intFrameSize = ((intFrameSize + intPadding) >> 2);
			break;
		case 2:
            intFrameSize  = intBitrateTable[intLSF][1][intBitrateIndex] * 144000;  
            intFrameSize /= intSamplingRateTable[intVersionID][intSamplingFreq];
            intFrameSize += intPadding;
			break;
		case 3:
			intFrameSize  = intBitrateTable[intLSF][2][intBitrateIndex] * 144000;
			intFrameSize /= intSamplingRateTable[intVersionID][intSamplingFreq]<<(intLSF);
			intFrameSize += intPadding;
			
			//计算帧边信息长度
			if(intVersionID == MPEG1)
				intSideInfoSize = (intMode == 3) ? 17 : 32;
			else {
				intSideInfoSize = (intMode == 3) ? 9 : 17;
			}
			break;
		}
		
		/*
		 * 计算主数据长度
		 */
		intMainDataBytes = intFrameSize - 4 -intSideInfoSize;
		if(intProtectionBit == 0)
			intMainDataBytes -= 2;
		
	}

}
