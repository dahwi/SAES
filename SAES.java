import java.util.*;
/**
 *This class implements simplified AES.
 *@author Dahwi Kim
 *@version 3/12/2017 
**/
public class SAES
{
    private static int[][] Sbox =  
        {
            {0x9,0x4,0xA,0xB},
            {0xD,0x1,0x8,0x5},
            {0x6,0x2,0x0,0x3},
            {0xC,0xE,0xF,0x7}
        };
    private static int[][] InSbox =
        {
            {0xA,0x5,0x9,0xB},
            {0x1,0x7,0x8,0xF},
            {0x6,0x0,0x2,0x3},
            {0xC,0x4,0xD,0xE}
        };
    private static int[][] AddGF16 = new int[16][16];
    static
    {
        for (int i = 0; i < AddGF16.length; i++)
            for (int j = 0; j < AddGF16[i].length; j++)
                AddGF16[i][j] = i ^ j;
		//check if it returns the rigth addition table
        //         for (int i = 0; i < add.length; i++)
        //         {
        //             for (int j = 0; j < add[i].length; j++)
        //                 System.out.print(Integer.toString(add[i][j],16) + " ");
        //             System.out.println();
        //         }
    }
    private static int[][] MulGF16 = 
        {
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0,   0,   0,   0,   0,   0,   0},
            {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xa, 0xb, 0xc, 0xd, 0xe, 0xf}, 
            {0, 2, 4, 6, 8, 0xa, 0xc, 0xe, 3, 1, 7, 5, 0xb, 9, 0xf, 0xd},
            {0, 3, 6, 5, 0xc, 0xf, 0xa, 9, 0xb, 8, 0xd, 0xe, 7, 4, 1, 2},
            {0, 4, 8, 0xc, 3, 7, 0xb, 0xf, 6, 2, 0xe, 0xa, 5, 1, 0xd, 9},
            {0, 5, 0xa, 0xf, 7, 2, 0xd, 8, 0xe, 0xb, 4, 1, 9, 0xc, 3, 6},
            {0, 6, 0xc, 0xa, 0xb, 0xd, 7, 1, 5, 3, 9, 0xf, 0xe, 8, 2, 4},
            {0, 7, 0xe, 9, 0xf, 8, 1, 6, 0xd, 0xa, 3, 4, 2, 5, 0xc, 0xb},
            {0, 8, 3, 0xb, 6, 0xe, 5, 0xd, 0xc, 4, 0xf, 7, 0xa, 2, 9, 1},
            {0, 9, 1, 8, 2, 0xb, 3, 0xa, 4, 0xd, 5, 0xc, 6, 0xf, 7, 0xe},
            {0, 0xa, 7, 0xd, 0xe, 4, 9, 3, 0xf, 5, 8, 2, 1, 0xb, 0xc, 6},
            {0, 0xb, 5, 0xe, 0xa, 1, 0xf, 4, 7, 0xc, 2, 9, 0xd, 6, 8, 3},
            {0, 0xc, 0xb, 7, 5, 9, 0xe, 2, 0xa, 6, 1, 0xd, 0xf, 3, 4, 8},
            {0, 0xd, 9, 4, 1, 0xc, 8, 5, 2, 0xf, 0xb, 6, 3, 0x3, 0xa, 7},
            {0, 0xe, 0xf, 1, 0xd, 3, 2, 0xc, 9, 7, 6, 8, 4, 0xa, 0xb, 5},
            {0, 0xf, 0xd, 2, 9, 6, 4, 0xb, 1, 0xe, 0xc, 3, 8, 7, 5, 0xa}
        };
    private static int[][] MC ={{1,4},{4,1}};

    private static int[][] InverseMC = {{9,2},{2,9}};

    public static void main(String[] args)
    {
		//Encrypt
        System.out.println(encrypt("0110111101101011","1010011100111011"));
        //Decrypt
		System.out.println(decrypt("0000011100111000","1010011100111011"));
    }

    public static ArrayList<String> keyGenerate(String key)
    {
		//Create an arraylist that stores six keys 
        ArrayList<String> keylist = new ArrayList<String>();
        String w0, w1;
        String w2="", w3="", w4="", w5=""; 

        w0=key.substring(0,8);
        w1=key.substring(8,16);
        keylist.add(w0);
        keylist.add(w1);
		//bit wise XOR the key segments and the output of the gfunciton of key segments
        for (int i = 0; i < w0.length(); i++)
        {
            if (w0.charAt(i) == gFunction(w1,1).charAt(i))
                w2 += "0";
            else
                w2 += "1";
        }
        keylist.add(w2);
        for (int i = 0; i < w0.length(); i++)
        {
            if (w2.charAt(i) == w1.charAt(i))
                w3 += "0";
            else
                w3 += "1";
        }
        keylist.add(w3);
        for (int i = 0; i < w2.length(); i++)
        {
            if (w2.charAt(i) == gFunction(w3,2).charAt(i))
                w4 += "0";
            else
                w4 += "1";
        }
        keylist.add(w4);
        for (int i = 0; i < w0.length(); i++)
        {
            if (w3.charAt(i) == w4.charAt(i))
                w5 += "0";
            else
                w5 += "1";
        }
        keylist.add(w5);

        return keylist;
    }

    private static String gFunction(String key, int n)
    {
        String R = key.substring(0,4); String L = key.substring(4,8);
        //substitute the nibble using Sbox
        int row1 = Integer.parseInt(L.substring(0,2),2);
        int col1 = Integer.parseInt(L.substring(2,4),2);
        int row2 = Integer.parseInt(R.substring(0,2),2);
        int col2 = Integer.parseInt(R.substring(2,4),2);
        String newkeyR=Integer.toString(Sbox[row1][col1],2);
        String newkeyL=Integer.toString(Sbox[row2][col2],2);
		//Make it into 4-bit String
        while(newkeyR.length()<4)
        {
            newkeyR="0"+newkeyR;
        }
        while(newkeyL.length()<4)
        {
            newkeyL="0"+newkeyL;
        }
        String newkey = newkeyR+newkeyL;
        //XOR with round constant
        String rcon1="10000000", rcon2="00110000"; String g;

        if (newkey.length() != rcon1.length())
            throw new RuntimeException();

        String ans = "";
        if(n==1)
        {
            for (int i = 0; i < newkey.length(); i++)
            {
				//bitwise XOR
                if (rcon1.charAt(i) == newkey.charAt(i))
                    ans += "0";
                else
                    ans += "1";
            }
        }
        else
        {
            for (int i = 0; i < newkey.length(); i++)
            {
				//bitwise XOR
                if (rcon2.charAt(i) == newkey.charAt(i))
                    ans += "0";
                else
                    ans += "1";
            }
        }

        return ans;
    }
    public static String addRoundkey(String plaintext, ArrayList<String> list, int n)
    {
        String ans="";;
        String message;
        int index = 2*(n-1) %6;
        ArrayList<String> mlist = new ArrayList<String>();

        //Transform the plaintext into 4bit string segments
        for (int start = 0, end = 4; end <= plaintext.length(); start += 4, end += 4)
        {
            message = plaintext.substring(start, end);
            mlist.add(message);
        }
        //Transform the first two key into 4bit string segments
        String key1R=list.get(index).substring(0,4);
        String key1L=list.get(index).substring(4,8);
        String key2R=list.get(index+1).substring(0,4);
        String key2L=list.get(index+1).substring(4,8);

        //bit wise XOR the plaintext segments and the key segments
        for (int i = 0; i < mlist.get(0).length(); i++)
        {
            if (mlist.get(0).charAt(i) == key1R.charAt(i))
                ans += "0";
            else
                ans += "1";
        }
        for (int i = 0; i < mlist.get(1).length(); i++)
        {
            if (mlist.get(1).charAt(i) == key1L.charAt(i))
                ans += "0";
            else
                ans += "1";
        }
        for (int i = 0; i < mlist.get(2).length(); i++)
        {
            if (mlist.get(2).charAt(i) == key2R.charAt(i))
                ans += "0";
            else
                ans += "1";
        }
        for (int i = 0; i < mlist.get(3).length(); i++)
        {
            if (mlist.get(3).charAt(i) == key2L.charAt(i))
                ans += "0";
            else
                ans += "1";
        }
        return ans;
    }

    public static String nibbleSub(String string)
    {
        String ans="";;
        String message;
        ArrayList<String> mlist = new ArrayList<String>();
		//Transform the input into 4bit string segments
        for (int start = 0, end = 4; end <= string.length(); start += 4, end += 4)
        {
            message = string.substring(start, end);
            mlist.add(message);
        }
        for(int i=0;i<4;i++)
        {
			//leftmost 2-bit represents row of the S-box,
            int row = Integer.parseInt(mlist.get(i).substring(0,2),2);
			//rightmost 2-bit represents column of the S-box
            int col = Integer.parseInt(mlist.get(i).substring(2,4),2);
			//Gets the corresponding value of the S-box and change it into 4-bit String
            String nString=Integer.toString(Sbox[row][col],2);
            while(nString.length()<4)
            {
                nString="0"+nString;
            }
            ans+=nString;
        }
        return ans;
    }
    
    public static String InvNibbleSub(String string)
    {
        String ans="";;
        String message;
        ArrayList<String> mlist = new ArrayList<String>();
		//Transform the input into 4bit string segments
        for (int start = 0, end = 4; end <= string.length(); start += 4, end += 4)
        {
            message = string.substring(start, end);
            mlist.add(message);
        }
        for(int i=0;i<4;i++)
        {
			//leftmost 2-bit represents row of the S-box, 
            int row = Integer.parseInt(mlist.get(i).substring(0,2),2);
			//rightmost 2-bit represents column of the S-box
            int col = Integer.parseInt(mlist.get(i).substring(2,4),2);
			//Gets the corresponding value of the S-box and change it into 4-bit String
            String nString=Integer.toString(InSbox[row][col],2);
            while(nString.length()<4)
            {
                nString="0"+nString;
            }
            ans+=nString;
        }
        return ans;
    }

    public static String shiftRow(String string)
    {
        String ans="";;
        String message;
        ArrayList<String> mlist = new ArrayList<String>(); 
		//Transform the input into 4bit string segments
        for (int start = 0, end = 4; end <= string.length(); start += 4, end += 4)
        {
            message = string.substring(start, end);
            mlist.add(message);
        }
		//Perform a one-nibble circular shift of the secod row of State, first row stays the same
        ans=mlist.get(0)+mlist.get(3)+mlist.get(2)+mlist.get(1);
        return ans;
    }

    public static String mixColumn(String string)
    {
        String ans="";;
        String message;
        ArrayList<String> mlist = new ArrayList<String>();
        int[][] matrix = new int[2][2];
        int r,c;
		//Transform the input into 4bit string segments and copy it into 2*2 array
        for (int start = 0, end = 4; end <= string.length(); start += 4, end += 4)
        {
            message = string.substring(start, end);
            r= (start% 8 == 0)? 0:1;
            c= (start<8)? 0:1;
            matrix[r][c]=Integer.parseInt(message,2);
        }
		//Perform matrix multiplication in GF(2^4) using the look up tables
        int[][] prod = new int[2][2];
        prod[0][0] = AddGF16[MulGF16[MC[0][0]][matrix[0][0]]][MulGF16[MC[0][1]][matrix[1][0]]];
        prod[0][1] = AddGF16[MulGF16[MC[0][0]][matrix[0][1]]][MulGF16[MC[0][1]][matrix[1][1]]];
        prod[1][0] = AddGF16[MulGF16[MC[1][0]][matrix[0][0]]][MulGF16[MC[1][1]][matrix[1][0]]];
        prod[1][1] = AddGF16[MulGF16[MC[1][0]][matrix[0][1]]][MulGF16[MC[1][1]][matrix[1][1]]];

        for(int col =0; col<2; col++)
        {
            for(int row=0; row<2; row++)
            {
                String nString=Integer.toString(prod[row][col],2);
				//Make it into 4-bit String
                while(nString.length()<4)
                {
                    nString="0"+nString;
                }
                ans+=nString;
            }
        }
        return ans;
    }
    
    public static String InvMixColumn(String string)
    {
        String ans="";;
        String message;
        ArrayList<String> mlist = new ArrayList<String>();
        int[][] matrix = new int[2][2];
        int r,c;
		//Transform the input into 4bit string segments and copy it into 2*2 array
        for (int start = 0, end = 4; end <= string.length(); start += 4, end += 4)
        {
            message = string.substring(start, end);
            r= (start% 8 == 0)? 0:1;
            c= (start<8)? 0:1;
            matrix[r][c]=Integer.parseInt(message,2);
        }

		//Perform matrix multiplication in GF(2^4) using the look up tables
        int[][] prod = new int[2][2];
        prod[0][0] = AddGF16[MulGF16[InverseMC[0][0]][matrix[0][0]]][MulGF16[InverseMC[0][1]][matrix[1][0]]];
        prod[0][1] = AddGF16[MulGF16[InverseMC[0][0]][matrix[0][1]]][MulGF16[InverseMC[0][1]][matrix[1][1]]];
        prod[1][0] = AddGF16[MulGF16[InverseMC[1][0]][matrix[0][0]]][MulGF16[InverseMC[1][1]][matrix[1][0]]];
        prod[1][1] = AddGF16[MulGF16[InverseMC[1][0]][matrix[0][1]]][MulGF16[InverseMC[1][1]][matrix[1][1]]];

        for(int col =0; col<2; col++)
        {
            for(int row=0; row<2; row++)
            {
                String nString=Integer.toString(prod[row][col],2);
				//Make it into 4-bit String
                while(nString.length()<4)
                {
                    nString="0"+nString;
                }
                ans+=nString;
            }
        }
        return ans;
    }

    public static String encrypt(String plaintext, String key)
    {
        ArrayList<String> keylist = keyGenerate(key);
        return addRoundkey(shiftRow(nibbleSub(addRoundkey(mixColumn(shiftRow(nibbleSub(addRoundkey(plaintext, keylist,1)))), keylist, 2))),keylist,3);
    }

    public static String decrypt(String ciphertext, String key)
    {
        ArrayList<String> keylist = keyGenerate(key);
        return addRoundkey(InvNibbleSub(shiftRow(InvMixColumn(addRoundkey(InvNibbleSub(shiftRow(addRoundkey(ciphertext, keylist,3))), keylist, 2)))),keylist,1);
    }

}