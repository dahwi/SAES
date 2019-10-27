public class MerkelHellman
{
    
    public static void main(String[] args)
    {
    }
    
    public static void keyGenerate(int[] seq, int s, int r)
    {
        int[] k =new int[9];
        
    }
    
    private static int[] P(int[] a)
    {
        int[] b = new int[9];
        for(int i = 0; i<3; i++)
            b[i] = a[i+6];
        for(int i = 3; i<6; i++)    
            b[i] = a[i];
        for(int i = 0; i<3; i++)
            b[i+6] = a[2-i];
        return b;
    }
}