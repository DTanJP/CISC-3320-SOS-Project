package cisc3320.davidtan.spring2017;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   sos.java


import java.io.PrintStream;

public class Format {

	public Format(String s) {
		width = 0;
		precision = -1;
		pre = "";
		post = "";
		leading_zeroes = false;
		show_plus = false;
		alternate = false;
		show_space = false;
		left_align = false;
		fmt = ' ';
		int i = s.length();
		int j = 0;
		int k;
		for(k = 0; j == 0; k++)
			if(k >= i)
				j = 5;
			else if(s.charAt(k) == '%') {
				if(k < i - 1) {
					if(s.charAt(k + 1) == '%') {
						pre = pre + '%';
						k++;
					} else {
						j = 1;
					}
				} else {
					throw new IllegalArgumentException();
				}
			} else {
				pre = pre + s.charAt(k);
			}

		while(j == 1) {
			if(k >= i)
				j = 5;
			else if(s.charAt(k) == ' ')
				show_space = true;
			else if(s.charAt(k) == '-')
				left_align = true;
			else if(s.charAt(k) == '+')
				show_plus = true;
			else if(s.charAt(k) == '0')
				leading_zeroes = true;
			else if(s.charAt(k) == '#') {
				alternate = true;
			} else {
				j = 2;
				k--;
			}
			k++;
		}
		while(j == 2) 
			if(k >= i)
				j = 5;
			else
				if(s.charAt(k) >= '0' && s.charAt(k) <= '9')
				{
					width = (width * 10 + s.charAt(k)) - 48;
					k++;
				} else
					if(s.charAt(k) == '.')
					{
						j = 3;
						precision = 0;
						k++;
					} else
					{
						j = 4;
					}
		while(j == 3) 
			if(k >= i)
				j = 5;
			else
				if(s.charAt(k) >= '0' && s.charAt(k) <= '9')
				{
					precision = (precision * 10 + s.charAt(k)) - 48;
					k++;
				} else
				{
					j = 4;
				}
		if(j == 4) {
			fmt = s.charAt(k);
			k++;
		}
		if(k < i)
			post = s.substring(k, i);
	}

	public static double atof(String s) {
		int i = 0;
		int j = 1;
		double d = 0.0D;
		double d2 = 1.0D;
		boolean flag = false;
		for(; i < s.length() && Character.isWhitespace(s.charAt(i)); i++);
		if(i < s.length() && s.charAt(i) == '-') {
			j = -1;
			i++;
		} else if(i < s.length() && s.charAt(i) == '+')
			i++;
		for(; i < s.length(); i++) {
			int k = s.charAt(i);
			if(k >= 48 && k <= 57)
			{
				if(!flag)
					d = (d * 10D + (double)k) - 48D;
				else
					if(flag)
					{
						d2 /= 10D;
						d += d2 * (double)(k - 48);
					}
			} else
				if(k == 46)
				{
					if(!flag)
						flag = true;
					else
						return (double)j * d;
				} else
					if(k == 101 || k == 69)
					{
						long l = (int)parseLong(s.substring(i + 1), 10);
						return (double)j * d * Math.pow(10D, l);
					} else
					{
						return (double)j * d;
					}
		}

		return (double)j * d;
	}

	public static int atoi(String s)
	{
		return (int)atol(s);
	}

	public static long atol(String s)
	{
		int i;
		for(i = 0; i < s.length() && Character.isWhitespace(s.charAt(i)); i++);
		if(i < s.length() && s.charAt(i) == '0')
		{
			if(i + 1 < s.length() && (s.charAt(i + 1) == 'x' || s.charAt(i + 1) == 'X'))
				return parseLong(s.substring(i + 2), 16);
			else
				return parseLong(s, 8);
		} else
		{
			return parseLong(s, 10);
		}
	}

	private static String convert(long l, int i, int j, String s)
	{
		if(l == 0L)
			return "0";
		String s1 = "";
		for(; l != 0L; l >>>= i)
			s1 = s.charAt((int)(l & (long)j)) + s1;

		return s1;
	}

	private String exp_format(double d)
	{
		String s = "";
		int i = 0;
		double d1 = d;
		double d2 = 1.0D;
		for(; d1 > 10D; d1 /= 10D)
		{
			i++;
			d2 /= 10D;
		}

		for(; d1 < 1.0D; d1 *= 10D)
		{
			i--;
			d2 *= 10D;
		}

		if((fmt == 'g' || fmt == 'G') && i >= -4 && i < precision)
			return fixed_format(d);
		d *= d2;
		s = s + fixed_format(d);
		if(fmt == 'e' || fmt == 'g')
			s = s + "e";
		else
			s = s + "E";
		String s1 = "000";
		if(i >= 0)
		{
			s = s + "+";
			s1 = s1 + i;
		} else
		{
			s = s + "-";
			s1 = s1 + -i;
		}
		return s + s1.substring(s1.length() - 3, s1.length());
	}

	private String fixed_format(double d)
	{
		String s = "";
		if(d > 9.2233720368547758E+018D)
			return exp_format(d);
		long l = (long)(precision != 0 ? d : d + 0.5D);
		s = s + l;
		double d1 = d - (double)l;
		if(d1 >= 1.0D || d1 < 0.0D)
			return exp_format(d);
		else
			return s + frac_part(d1);
	}

	public String form(char c)
	{
		if(fmt != 'c')
		{
			throw new IllegalArgumentException();
		} else
		{
			String s = String.valueOf(c);
			return pad(s);
		}
	}

	public String form(double d)
	{
		if(precision < 0)
			precision = 6;
		byte byte0 = 1;
		if(d < 0.0D)
		{
			d = -d;
			byte0 = -1;
		}
		String s;
		if(fmt == 'f')
			s = fixed_format(d);
		else
			if(fmt == 'e' || fmt == 'E' || fmt == 'g' || fmt == 'G')
				s = exp_format(d);
			else
				throw new IllegalArgumentException();
		return pad(sign(byte0, s));
	}

	public String form(long l)
	{
		byte byte0 = 0;
		String s;
		if(fmt == 'd' || fmt == 'i')
		{
			byte0 = 1;
			if(l < 0L)
			{
				l = -l;
				byte0 = -1;
			}
			s = String.valueOf(l);
		} else
			if(fmt == 'o')
				s = convert(l, 3, 7, "01234567");
			else
				if(fmt == 'x')
					s = convert(l, 4, 15, "0123456789abcdef");
				else
					if(fmt == 'X')
						s = convert(l, 4, 15, "0123456789ABCDEF");
					else
						throw new IllegalArgumentException();
		return pad(sign(byte0, s));
	}

	public String form(String s)
	{
		if(fmt != 's')
			throw new IllegalArgumentException();
		if(precision >= 0)
			s = s.substring(0, precision);
		return pad(s);
	}

	private String frac_part(double d)
	{
		String s = "";
		if(precision > 0)
		{
			double d1 = 1.0D;
			String s1 = "";
			for(int j = 1; j <= precision && d1 <= 9.2233720368547758E+018D; j++)
			{
				d1 *= 10D;
				s1 = s1 + "0";
			}

			long l = (long)(d1 * d + 0.5D);
			s = s1 + l;
			s = s.substring(s.length() - precision, s.length());
		}
		if(precision > 0 || alternate)
			s = "." + s;
		if((fmt == 'G' || fmt == 'g') && !alternate)
		{
			int i;
			for(i = s.length() - 1; i >= 0 && s.charAt(i) == '0'; i--);
			if(i >= 0 && s.charAt(i) == '.')
				i--;
			s = s.substring(0, i + 1);
		}
		return s;
	}

	private String pad(String s)
	{
		String s1 = repeat(' ', width - s.length());
		if(left_align)
			return pre + s + s1 + post;
		else
			return pre + s1 + s + post;
	}

	private static long parseLong(String s, int i)
	{
		int j = 0;
		int k = 1;
		long l = 0L;
		for(; j < s.length() && Character.isWhitespace(s.charAt(j)); j++);
		if(j < s.length() && s.charAt(j) == '-')
		{
			k = -1;
			j++;
		} else
			if(j < s.length() && s.charAt(j) == '+')
				j++;
		for(; j < s.length(); j++)
		{
			int i1 = s.charAt(j);
			if(i1 >= 48 && i1 < 48 + i)
				l = (l * (long)i + (long)i1) - 48L;
			else
				if(i1 >= 65 && i1 < (65 + i) - 10)
					l = ((l * (long)i + (long)i1) - 65L) + 10L;
				else
					if(i1 >= 97 && i1 < (97 + i) - 10)
						l = ((l * (long)i + (long)i1) - 97L) + 10L;
					else
						return l * (long)k;
		}

		return l * (long)k;
	}

	public static void print(PrintStream printstream, String s, char c)
	{
		printstream.print((new Format(s)).form(c));
	}

	public static void print(PrintStream printstream, String s, double d)
	{
		printstream.print((new Format(s)).form(d));
	}

	public static void print(PrintStream printstream, String s, long l)
	{
		printstream.print((new Format(s)).form(l));
	}

	public static void print(PrintStream printstream, String s, String s1)
	{
		printstream.print((new Format(s)).form(s1));
	}

	private static String repeat(char c, int i)
	{
		if(i <= 0)
			return "";
		StringBuffer stringbuffer = new StringBuffer(i);
		for(int j = 0; j < i; j++)
			stringbuffer.append(c);

		return stringbuffer.toString();
	}

	private String sign(int i, String s)
	{
		String s1 = "";
		if(i < 0)
			s1 = "-";
		else
			if(i > 0)
			{
				if(show_plus)
					s1 = "+";
				else
					if(show_space)
						s1 = " ";
			} else
				if(fmt == 'o' && alternate && s.length() > 0 && s.charAt(0) != '0')
					s1 = "0";
				else
					if(fmt == 'x' && alternate)
						s1 = "0x";
					else
						if(fmt == 'X' && alternate)
							s1 = "0X";
		int j = 0;
		if(leading_zeroes)
			j = width;
		else
			if((fmt == 'd' || fmt == 'i' || fmt == 'x' || fmt == 'X' || fmt == 'o') && precision > 0)
				j = precision;
		return s1 + repeat('0', j - s1.length() - s.length()) + s;
	}

	private int width;
	private int precision;
	private String pre;
	private String post;
	private boolean leading_zeroes;
	private boolean show_plus;
	private boolean alternate;
	private boolean show_space;
	private boolean left_align;
	private char fmt;
}
