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
	
	private static int intVersionID;//�汾��2λ��00-MPEG2.5��01-δ���壬10-MPEG2��11-MPEG1
	private static int intLayer;//�㣬MPEG����Ϊ3�㣬�����������ӣ�2λ��00-δ���壬01-Layer3��10-Layer2��11-Layer1
	private static int intProtectionBit;//CRCУ�飬1λ��0-У�飬1-��У��
	private static int intBitrateIndex;//�����ݵ�λ��
	private static int intSamplingFreq;//PCM������
	private static int intPadding;//�ֽ����λ����1��Ϊ��1�ֽ����
	private static int intPrivate;
	private static int intMode;//����ģʽ��0-��������1-������������2-˫������3-������
	private static int intModeExtension;//��ʾ��������������������ʽ
	private static int intCopyright;//��Ȩ
	private static int intOriginal;//�Ƿ���ԭ������
	private static int intEmphasis;//Ԥ����
	
	private static int intFrameSize;
	private static int intMainDataBytes;//�����ݳ���
	private static int intSideInfoSize;//����Ϣ����
	private static int intLSF;
	private static int intStandardMask = 0xffe00000;
	private static boolean boolMS_Stereo,boolIntensity_Stereo;
	private static IRandomAccess iraInput;
	
	/*
	 * ���캯��
	 */
	public MP3Header(IRandomAccess in_rai){
		iraInput = in_rai;
	}
	
	
	/*
	 * ֡ͷ����
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
		 * �ɱ���Layer��ص���Ϣ
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
			
			//����֡����Ϣ����
			if(intVersionID == MPEG1)
				intSideInfoSize = (intMode == 3) ? 17 : 32;
			else {
				intSideInfoSize = (intMode == 3) ? 9 : 17;
			}
			break;
		}
		
		/*
		 * ���������ݳ���
		 */
		intMainDataBytes = intFrameSize - 4 -intSideInfoSize;
		if(intProtectionBit == 0)
			intMainDataBytes -= 2;
		
	}

}
