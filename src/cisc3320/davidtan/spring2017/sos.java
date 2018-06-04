package cisc3320.davidtan.spring2017;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   sos.java


import cisc3320.davidtan.spring2017.os;

public class sos {

	public sos() {
		Next = new int[6];
		Parms = new int[6];
		JobTable = new jobtype[52];
		CoreMap = new int[22][4];
		SizeDist = new int[30];
		MCpuTimeDist = new int[30];
		TCpuTimeDist = new int[30];
		PriorDist = new int[30];
		Times = new int[3][31][10];
		WhichSvc = new int[31][10];
		DrmTimes = new int[10];
		DskTimes = new int[10];
		CrdTimes = new int[10];
		Action = new int[1];
	}

	static int coreused() {
		int i = 0;
		for(int k = 1; k <= 20; k++)
			if(CoreMap[k][1] > 0)
				i = ((i + CoreMap[k][3]) - CoreMap[k][2]) + 1;

		int j = i;
		return j;
	}

	static void error(int i) {
		err = true;
		if(i == 13)
			err13 = true;
		System.out.println("\n\n\n*** Clock:  " + Clock + ", *** FATAL ERROR:  " + i + "\n");
		System.out.println(errorarray[i] + "\n");
		System.out.println("Current Value of Registers:\n\n\ta = " + Action[0]);
		System.out.print("\tp [1..5] = ");
		for(int j = 1; j <= 5; j++)
			System.out.print(" " + Parms[j] + "  ");

		System.out.println("\n");
		Statistics();
	}

	static void FindNextEvent() {
		int k = 0;
		int l = 0x1312d00;
		for(int i = 1; i <= 3; i++)
			if(l > Next[i]) {
				k = i;
				l = Next[i];
			}

		if(Action[0] == 2) {
			for(int j = 5; j >= 4; j--)
				if(l >= Next[j] + Clock) {
					k = j;
					l = Next[j] + Clock;
				}

			JobTable[jti].CpuTimeUsed = (JobTable[jti].CpuTimeUsed + l) - Clock;
			CpuUtil = (CpuUtil + (double)l) - (double)Clock;
		}
		Clock = l;
		Action[0] = k;
	}

	static void GenCrint() {
		int i;
		for(i = 1; i <= 50 && !JobTable[i].Overwrite; i++);
		if(i > 50)
			error(4);
		if(!err) {
			JobCntr++;
			int j = JobCntr % 30;
			JobTable[i].JobNo = JobCntr;
			JobTable[i].Size = SizeDist[j];
			JobTable[i].StartTime = Clock;
			JobTable[i].CpuTimeUsed = 0;
			JobTable[i].MaxCpuTime = MCpuTimeDist[j];
			JobTable[i].TermCpuTime = TCpuTimeDist[j];
			JobTable[i].NextSvc = 0;
			JobTable[i].IOPending = 0;
			JobTable[i].IOComp = 0;
			JobTable[i].Priority = PriorDist[j];
			if(JobTable[i].MaxCpuTime < 100 || JobTable[i].TermCpuTime < 100)
				JobTable[i].JobType = 1;
			else
				JobTable[i].JobType = 2;
			JobTable[i].Blocked = false;
			JobTable[i].Latched = false;
			JobTable[i].InCore = false;
			JobTable[i].Terminated = false;
			JobTable[i].Overwrite = false;
			Parms[1] = JobTable[i].JobNo;
			Parms[2] = JobTable[i].Priority;
			Parms[3] = JobTable[i].Size;
			Parms[4] = JobTable[i].MaxCpuTime;
			Parms[5] = Clock;
			CardTimesPtr++;
			int k = CardTimesPtr % 10;
			Next[1] = CrdTimes[k] + Clock;
			if(trace) {
				System.out.print("*** Clock:  " + Parms[5]);
				System.out.print(", Job " + Parms[1] + " Arriving ");
				System.out.print("Size:  " + Parms[3]);
				System.out.println(" Priority:  " + Parms[2]);
				System.out.println(" Max CPU Time:  " + Parms[4] + "\n");
			}
			if(JobTable[i].MaxCpuTime <= 0 || JobTable[i].Size <= 0 || JobTable[i].Size > 100) {
				if(trace)
					System.out.println(" But job deleted due to max cpu time or size.");
				JobTable[i].Overwrite = true;
				DelCntr++;
			}
		}
	}

	static void GenDrmint()
	{
		if(trace)
		{
			System.out.print("*** Clock:  " + Clock + ", Swap ");
			if(writ)
				System.out.print("out");
			else
				System.out.print("in");
			System.out.println(" completed for job " + JobSwpdNo);
		}
		if(!writ)
		{
			int i;
			for(i = 1; i < 50 && JobTable[i].JobNo != JobSwpdNo; i++);
			JobTable[i].InCore = true;
			int j = coreused();
			CoreUtil += (double)j * ((double)Clock - LstCrChk);
			LstCrChk = Clock;
			CoreMap[cmi][1] = JobSwpdNo;
			CoreMap[cmi][2] = CoreAddr;
			CoreMap[cmi][3] = (CoreAddr + Sze) - 1;
			PutCoreMap();
		}
		DrumBusy = false;
		DrumUtil = (DrumUtil + (double)Clock) - (double)DrmStTm;
		Next[3] = 0x1b7740;
		Action[0] = 3;
		Parms[5] = Clock;
	}

	static void GenDskint()
	{
		int k = JobServdIndex;
		if(trace)
		{
			System.out.print("*** Clock:  " + Clock + ", IO Completion");
			System.out.println(" for job " + JobTable[k].JobNo);
		}
		DiskBusy = false;
		JobTable[k].Latched = false;
		if((double)JobTable[k].IOPending == 1.0D)
			JobTable[k].Blocked = false;
		JobTable[k].IOPending = JobTable[k].IOPending - 1;
		JobTable[k].IOComp = JobTable[k].IOComp + 1;
		DiskUtil = (DiskUtil + (double)Clock) - (double)DskStTm;
		Next[2] = 0x1b7740;
		Action[0] = 2;
		Parms[5] = Clock;
		if(JobTable[k].Terminated && (double)JobTable[k].IOPending == 0.0D)
		{
			int l = coreused();
			CoreUtil += (double)l * ((double)Clock - LstCrChk);
			LstCrChk = Clock;
			int i;
			for(i = 1; i <= 21 && JobTable[k].JobNo != CoreMap[i][1]; i++);
			if(i > 21)
				error(39);
			if(!err)
			{
				for(int j = 1; j <= 3; j++)
					CoreMap[i][j] = 0;

				JobTable[k].Overwrite = true;
				JobTable[k].InCore = false;
			}
		}
	}

	static void GenSvc() {
		Parms[5] = Clock;
		int i;
		if(JobTable[jti].TermCpuTime <= JobTable[jti].CpuTimeUsed) {
			i = 0;
		} else {
			byte byte0 = 31;
			i = WhichSvc[jti % byte0][JobTable[jti].NextSvc % 10];
		}
		if(trace) {
			System.out.print("*** Clock:  " + Clock + ", Job " + JobTable[jti].JobNo);
			switch(i) {
			case 0: // '\0'
				System.out.print(" terminate ");
				break;

			case 1: // '\001'
				System.out.print(" block ");
				break;

			case 2: // '\002'
				System.out.print(" I/O ");
				break;
			}
			System.out.println("Svc issued");
		}
		if(i >= 0 && i <= 2)
			switch(i) {
			case 0: // '\0'
				Action[0] = 5;
				CondCode = 1;
				SaveStatistics();
				return;

			case 1: // '\001'
				Action[0] = 7;
				if(JobTable[jti].IOPending > 0)
					JobTable[jti].Blocked = true;
				JobTable[jti].NextSvc = JobTable[jti].NextSvc + 1;
				return;

			case 2: // '\002'
				Action[0] = 6;
				JobTable[jti].IOPending = JobTable[jti].IOPending + 1;
				JobTable[jti].NextSvc = JobTable[jti].NextSvc + 1;
				return;
			}
		else
			error(38);
	}

	static void GenTro()
	{
		if(trace)
		{
			System.out.print("Clock:  " + Clock + ", time run out ");
			System.out.println("on Job " + JobTable[jti].JobNo);
		}
		if(JobTable[jti].CpuTimeUsed >= JobTable[jti].MaxCpuTime)
		{
			CondCode = 2;
			SaveStatistics();
		}
		Parms[5] = Clock;
	}

	static void Idle()
	{
		if(trace)
			System.out.println("*** Clock:  " + Clock + ", executive idling");
		boolean flag = false;
		boolean flag1 = false;
		for(int i = 1; i <= 50 && !flag1; i++)
			if(JobTable[i].InCore)
			{
				flag = true;
				if(!JobTable[i].Blocked && !JobTable[i].Terminated)
					flag1 = true;
			}

		if(flag1)
			error(15);
		if(!err && !DiskBusy && flag)
			error(16);
		if(!err && !DrumBusy && JobCntr > TermCntr && !flag)
			error(17);
	}

	static void init()
	{
		int ai[] = new int[10];
		DrmTimes[0] = 11;
		DrmTimes[1] = 17;
		DrmTimes[2] = 21;
		DrmTimes[3] = 19;
		DrmTimes[4] = 15;
		DrmTimes[5] = 23;
		DrmTimes[6] = 25;
		DrmTimes[7] = 13;
		DrmTimes[8] = 29;
		DrmTimes[9] = 27;
		DskTimes[0] = 55;
		DskTimes[1] = 85;
		DskTimes[2] = 105;
		DskTimes[3] = 95;
		DskTimes[4] = 75;
		DskTimes[5] = 115;
		DskTimes[6] = 125;
		DskTimes[7] = 65;
		DskTimes[8] = 145;
		DskTimes[9] = 135;
		CrdTimes[0] = 2800;
		CrdTimes[1] = 2600;
		CrdTimes[2] = 3300;
		CrdTimes[3] = 1400;
		CrdTimes[4] = 30;
		CrdTimes[5] = 10;
		CrdTimes[6] = 19;
		CrdTimes[7] = 2850;
		CrdTimes[8] = 2740;
		CrdTimes[9] = 4000;
		SizeDist[0] = 15;
		SizeDist[1] = 18;
		SizeDist[2] = 25;
		SizeDist[3] = 8;
		SizeDist[4] = 10;
		SizeDist[5] = 30;
		SizeDist[6] = 47;
		SizeDist[7] = 27;
		SizeDist[8] = 10;
		SizeDist[9] = 14;
		SizeDist[10] = 30;
		SizeDist[11] = 16;
		SizeDist[12] = 19;
		SizeDist[13] = 23;
		SizeDist[14] = 5;
		SizeDist[15] = 15;
		SizeDist[16] = 6;
		SizeDist[17] = 10;
		SizeDist[18] = 8;
		SizeDist[19] = 7;
		SizeDist[20] = 17;
		SizeDist[21] = 15;
		SizeDist[22] = 40;
		SizeDist[23] = 11;
		SizeDist[24] = 14;
		SizeDist[25] = 17;
		SizeDist[26] = 21;
		SizeDist[27] = 23;
		SizeDist[28] = 5;
		SizeDist[29] = 8;
		MCpuTimeDist[0] = 14000;
		MCpuTimeDist[1] = 23;
		MCpuTimeDist[2] = 2500;
		MCpuTimeDist[3] = 20;
		MCpuTimeDist[4] = 3500;
		MCpuTimeDist[5] = 14;
		MCpuTimeDist[6] = 65000;
		MCpuTimeDist[7] = 100;
		MCpuTimeDist[8] = 10;
		MCpuTimeDist[9] = 1500;
		MCpuTimeDist[10] = 11;
		MCpuTimeDist[11] = 10;
		MCpuTimeDist[12] = 550;
		MCpuTimeDist[13] = 1400;
		MCpuTimeDist[14] = 17;
		MCpuTimeDist[15] = 40000;
		MCpuTimeDist[16] = 19;
		MCpuTimeDist[17] = 1300;
		MCpuTimeDist[18] = 15;
		MCpuTimeDist[19] = 21;
		MCpuTimeDist[20] = 131;
		MCpuTimeDist[21] = 153;
		MCpuTimeDist[22] = 1000;
		MCpuTimeDist[23] = 32;
		MCpuTimeDist[24] = 18;
		MCpuTimeDist[25] = 5300;
		MCpuTimeDist[26] = 62;
		MCpuTimeDist[27] = 17;
		MCpuTimeDist[28] = 7100;
		MCpuTimeDist[29] = 15;
		for(int i = 0; i <= 14; i++)
		{
			PriorDist[2 * i] = 1;
			PriorDist[2 * i + 1] = 2;
		}

		PriorDist[6] = 5;
		PriorDist[7] = 1;
		TCpuTimeDist[0] = 4000;
		TCpuTimeDist[1] = 21;
		TCpuTimeDist[2] = 2000;
		TCpuTimeDist[3] = 20;
		TCpuTimeDist[4] = 4000;
		TCpuTimeDist[5] = 11;
		TCpuTimeDist[6] = 50000;
		TCpuTimeDist[7] = 90;
		TCpuTimeDist[8] = 9;
		TCpuTimeDist[9] = 100;
		TCpuTimeDist[10] = 10;
		TCpuTimeDist[11] = 12;
		TCpuTimeDist[12] = 500;
		TCpuTimeDist[13] = 1300;
		TCpuTimeDist[14] = 15;
		TCpuTimeDist[15] = 3000;
		TCpuTimeDist[16] = 15;
		TCpuTimeDist[17] = 1200;
		TCpuTimeDist[18] = 13;
		TCpuTimeDist[19] = 20;
		TCpuTimeDist[20] = 130;
		TCpuTimeDist[21] = 150;
		TCpuTimeDist[22] = 900;
		TCpuTimeDist[23] = 37;
		TCpuTimeDist[24] = 20;
		TCpuTimeDist[25] = 2500;
		TCpuTimeDist[26] = 60;
		TCpuTimeDist[27] = 14;
		TCpuTimeDist[28] = 3000;
		TCpuTimeDist[29] = 15;
		for(int j = 0; j <= 30; j++)
		{
			for(int j2 = 0; j2 <= 9; j2++)
				Times[1][j][j2] = 3 * (j2 + 1);

		}

		ai[1] = 4;
		ai[2] = 8;
		ai[3] = 11;
		ai[4] = 12;
		ai[5] = 17;
		ai[6] = 21;
		ai[7] = 24;
		ai[8] = 27;
		ai[9] = 30;
		for(int k = 1; k <= 9; k++)
		{
			for(int k2 = 0; k2 <= 9; k2++)
				Times[1][ai[k]][k2] = 500 * (k2 + 1);

		}

		for(int l = 0; l <= 14; l++)
		{
			for(int l2 = 0; l2 <= 9; l2++)
				Times[2][2 * l][l2] = 3 * (l2 + 1);

			for(int i3 = 0; i3 <= 9; i3++)
				Times[2][2 * l + 1][i3] = 500 * (i3 + 1);

		}

		for(int j3 = 0; j3 <= 9; j3++)
			Times[2][30][j3] = 3 * (j3 + 1);

		for(int i1 = 0; i1 <= 30; i1++)
		{
			for(int k3 = 0; k3 <= 9; k3++)
				WhichSvc[i1][k3] = 2 - k3 % 2;

		}

		for(int j1 = 1; j1 <= 51; j1++)
		{
			JobTable[j1] = new jobtype();
			JobTable[j1].JobNo = 0;
			JobTable[j1].Size = 0;
			JobTable[j1].StartTime = 0.0D;
			JobTable[j1].CpuTimeUsed = 0;
			JobTable[j1].MaxCpuTime = 0;
			JobTable[j1].TermCpuTime = 0;
			JobTable[j1].NextSvc = 0;
			JobTable[j1].IOPending = 0;
			JobTable[j1].IOComp = 0;
			JobTable[j1].Priority = 0;
			JobTable[j1].JobType = 0;
			JobTable[j1].Blocked = false;
			JobTable[j1].Latched = false;
			JobTable[j1].InCore = false;
			JobTable[j1].Terminated = false;
			JobTable[j1].Overwrite = true;
		}

		for(int k1 = 1; k1 <= 20; k1++)
		{
			for(int l3 = 1; l3 <= 3; l3++)
				CoreMap[k1][l3] = 0;

		}

		CoreMap[21][1] = -1;
		CoreMap[21][2] = 100;
		CoreMap[21][3] = 0x3b9aca00;
		Clock = 0;
		Action[0] = 1;
		Next[1] = 0;
		for(int l1 = 2; l1 <= 5; l1++)
			Next[l1] = 0xdbba1;

		JobCntr = 0;
		DelCntr = 0.0D;
		TermCntr = 0;
		DilCntr = 0.0D;
		DrumTimesPtr = 0;
		DiskTimesPtr = 0;
		CardTimesPtr = 0;
		AvgDil = 0.0D;
		AvgResponse = 0.0D;
		DrumUtil = 0.0D;
		DiskUtil = 0.0D;
		CpuUtil = 0.0D;
		CoreUtil = 0.0D;
		LstCrChk = 0.0D;
		AvgDisk = 0.0D;
		for(int i2 = 0; i2 <= 9; i2++)
			AvgDisk += DskTimes[i2];

		AvgDisk /= 10D;
		DiskBusy = false;
		DrumBusy = false;
		trace = false;
		LastSnap = 0.0D;
		SpstInt = 60000D;
		System.out.println("\n\n\t\t\tOPERATING SYSTEM SIMULATION\n\n");
	}

	public static void offtrace()
	{
		trace = false;
	}

	public static void ontrace()
	{
		trace = true;
	}

	static void PutCoreMap()
	{
		int ai[] = new int[100];
		if(trace)
		{
			for(int i = 0; i <= 99; i++)
				ai[i] = 0;

			for(int j = 1; j <= 20; j++)
				if(CoreMap[j][1] > 0)
				{
					for(int i1 = CoreMap[j][2]; i1 <= CoreMap[j][3]; i1++)
						ai[i1] = CoreMap[j][1];

				}

			System.out.println("\n\n\t\t\t\tCORE MAP\n");
			for(int k = 0; k < 4; k++)
				System.out.print(" Partition Job   ");

			System.out.println("\n");
			for(int l = 0; l < 25; l++)
			{
				if(l < 10)
					System.out.print(" ");
				System.out.print("     " + l + "\t   " + ai[l] + "\t      ");
				System.out.print((l + 25) + "    " + ai[l + 25] + "\t       ");
				System.out.print((l + 50) + "    " + ai[l + 50] + "  \t");
				System.out.println((l + 75) + "    " + ai[l + 75]);
			}

			System.out.println();
		}
	}

	static void Run()
	{
		int l = 1;
		int i1 = 1;
		for(; l < 21 && (Parms[2] != CoreMap[l][2] || Parms[3] != (CoreMap[l][3] - CoreMap[l][2]) + 1); l++);
		if(l >= 21)
			error(13);
		if(!err)
			for(; i1 <= 50 && CoreMap[l][1] != JobTable[i1].JobNo; i1++);
		if(!err && i1 > 50)
		{
			error(5);
			i1 = 1;
		}
		jti = i1;
		if(!err && !JobTable[jti].InCore)
		{
			System.err.println("JOBNO:  " + JobTable[jti].JobNo);
			error(6);
		}
		if(!err && JobTable[jti].Blocked)
		{
			System.err.println("JOBNO:  " + JobTable[jti].JobNo);
			error(10);
		}
		if(!err && JobTable[jti].Terminated)
		{
			System.err.println("JOBNO:  " + JobTable[jti].JobNo);
			error(11);
		}
		if(!err && Parms[4] + JobTable[jti].CpuTimeUsed > JobTable[jti].MaxCpuTime)
		{
			System.err.println("JOBNO:  " + JobTable[jti].JobNo);
			error(12);
		}
		if(!err && Parms[4] <= 0)
		{
			System.err.println("JOBNO:  " + JobTable[jti].JobNo);
			error(14);
		}
		if(!err)
		{
			Next[4] = Parms[4];
			int j = JobTable[jti].JobType;
			byte byte0 = 31;
			int k = jti % byte0;
			int i = JobTable[jti].NextSvc;
			int i2 = Times[j][k][9];
			int j2 = i / 10;
			int j1 = i % 10;
			int k2 = Times[j][k][j1];
			int l1 = i2 * j2 + k2;
			int k1 = JobTable[jti].TermCpuTime;
			if(l1 < k1)
				Next[5] = l1 - JobTable[jti].CpuTimeUsed;
			else
				Next[5] = k1 - JobTable[jti].CpuTimeUsed;
			if(trace)
			{
				System.out.print("*** Clock:  " + Clock + ", ");
				System.out.print("Job " + JobTable[jti].JobNo + " ");
				System.out.print("running size:  " + Parms[3]);
				System.out.println(" Priority:  " + JobTable[jti].Priority);
				System.out.print(" Max CPU Time:  " + JobTable[jti].MaxCpuTime + ", ");
				System.out.println("CPU time used:  " + JobTable[jti].CpuTimeUsed);
				System.out.println();
			}
		}
	}

	static void SaveStatistics()
	{
		if(JobTable[jti].CpuTimeUsed > 100)
		{
			double d = Times[JobTable[jti].JobType][jti % 31][0];
			double d1;
			if(d > AvgDisk)
			{
				d1 = JobTable[jti].CpuTimeUsed;
			} else
			{
				double d3 = d + AvgDisk;
				double d6 = d3;
				if(d3 - d6 >= 0.5D)
					d1 = (d + AvgDisk) * (double)(JobTable[jti].IOComp - 1) + d + 1.0D;
				else
					d1 = (d + AvgDisk) * (double)(JobTable[jti].IOComp - 1) + d;
			}
			double d2 = ((double)Clock - JobTable[jti].StartTime) / d1;
			System.out.print("\n*** Clock:  " + Clock + ", ");
			System.out.print("Job " + JobTable[jti].JobNo + " ");
			System.out.println("terminated " + CondMess[CondCode]);
			printf("Dilation:  %.2f ", d2);
			System.out.print("CPU time:  " + JobTable[jti].CpuTimeUsed);
			System.out.println("  # I/O operations completed:  " + JobTable[jti].IOComp);
			System.out.println(" # I/O operations pending:  " + JobTable[jti].IOPending);
			System.out.println();
			AvgDil += d2;
			DilCntr++;
		} else
		{
			double d4 = (double)Clock - JobTable[jti].StartTime;
			AvgResponse += d4;
			System.out.print("\n*** Clock:  " + Clock + ", ");
			System.out.print("Job " + JobTable[jti].JobNo + " ");
			System.out.println("terminated " + CondMess[CondCode]);
			printf(" Response Time:  %.0f ", d4);
			System.out.print("CPU time:  " + JobTable[jti].CpuTimeUsed);
			System.out.println(" # I/O operations completed:  " + JobTable[jti].IOComp);
			System.out.println(" # I/O operations pending:  " + JobTable[jti].IOPending);
			System.out.println();
		}
		TermCntr++;
		int i;
		for(i = 1; i < 21 && JobTable[jti].JobNo != CoreMap[i][1]; i++);
		if(i == 21)
			error(37);
		if(!err)
		{
			if(JobTable[jti].IOPending != 0)
			{
				JobTable[jti].Terminated = true;
			} else
			{
				JobTable[jti].Overwrite = true;
				JobTable[jti].InCore = false;
				JobTable[jti].Terminated = true;
				double d5 = coreused();
				CoreUtil += d5 * ((double)Clock - LstCrChk);
				LstCrChk = Clock;
				for(int j = 1; j <= 3; j++)
					CoreMap[i][j] = 0;

			}
			PutCoreMap();
		}
	}

	public static void main(String[] args) {
		new sos();
		init();
		os.startup();
		err = false;
		err13 = false;
		while(Clock < 0xdbba0 && !err) {
			FindNextEvent();
			if(Action[0] < 6 && Action[0] > 0)
				switch(Action[0]) {
				case 1: // '\001'
					GenCrint();
					break;

				case 2: // '\002'
					GenDskint();
					break;

				case 3: // '\003'
					GenDrmint();
					break;

				case 4: // '\004'
					GenTro();
					break;

				case 5: // '\005'
					GenSvc();
					break;
				}
			else
				error(1);
			if(!err && Action[0] < 8 && Action[0] > 0)
				switch(Action[0]) {
				case 1: // '\001'
					os.Crint(Action, Parms);
					break;

				case 2: // '\002'
					os.Dskint(Action, Parms);
					break;

				case 3: // '\003'
					os.Drmint(Action, Parms);
					break;

				case 4: // '\004'
					os.Tro(Action, Parms);
					break;

				case 5: // '\005'
				case 6: // '\006'
				case 7: // '\007'
					os.Svc(Action, Parms);
					break;
				}
			else
				if(!err)
					error(2);
			if(!err && (double)Clock - LastSnap >= SpstInt)
				SnapShot();
			if(!err)
				if(Action[0] == 1)
					Idle();
				else
					if(Action[0] == 2)
						Run();
					else
						error(3);
		}
		if(!err)
			Statistics();
	}

	public static void siodisk(int i) {
		int j = 1;
		if(trace)
			System.out.println("*** Clock:  " + Clock + ", Job " + i + " I/O started");
		for(; j <= 50 && JobTable[j].JobNo != i; j++);
		if(j > 50) {
			error(18);
			j = 1;
		}
		if(!err && !JobTable[j].InCore)
			error(19);
		if(!err && JobTable[j].Overwrite)
			error(20);
		if(!err && DiskBusy)
			error(21);
		if(!err && JobTable[j].IOPending == 0)
			error(22);
		if(!err)
		{
			DiskTimesPtr++;
			DskStTm = Clock;
			Next[2] = Clock + DskTimes[DiskTimesPtr % 10];
			JobTable[j].Latched = true;
			JobServdIndex = j;
			DiskBusy = true;
		}
	}

	public static void siodrum(int id, int size, int address, int l) {
		int i1 = 1;
		JobSwpdNo = id;
		Sze = size;
		CoreAddr = address;
		if(l == 1)
			writ = true;
		else
			writ = false;
		if(trace) {
			System.out.print("*** Clock:  " + Clock + ", Job " + JobSwpdNo);
			if(writ)
				System.out.print(" swapout started.  ");
			else
				System.out.print(" swapin started.  ");
			System.out.println("Size:  " + Sze);
			System.out.println(" Starting address:  " + CoreAddr + "\n");
		}
		PutCoreMap();
		if(DrumBusy)
			error(23);
		if(!err)
			for(; i1 <= 50 && JobTable[i1].JobNo != JobSwpdNo; i1++);
		if(i1 > 50)
		{
			error(24);
			i1 = 1;
		}
		if(!err && JobTable[i1].Size != size)
			error(25);
		if(!err && Sze == 0)
			error(36);
		if(!err && JobTable[i1].Overwrite)
			error(26);
		double d1 = (Sze + CoreAddr) - 1;
		if(writ) {
			if(!err && !JobTable[i1].InCore)
				error(27);
			if(!err && JobTable[i1].Latched)
				error(28);
			int j1 = 1;
			if(!err)
				for(; j1 <= 21 && CoreMap[j1][1] != JobSwpdNo; j1++);
			cmi = j1;
			if(cmi > 21)
			{
				error(29);
				cmi = 1;
			}
			if(!err && CoreAddr != CoreMap[cmi][2])
				error(30);
			if(!err && d1 != (double)CoreMap[cmi][3])
				error(31);
			if(!err)
			{
				JobTable[i1].InCore = false;
				double d = coreused();
				CoreUtil += d * ((double)Clock - LstCrChk);
				LstCrChk = Clock;
				CoreMap[cmi][1] = 0;
				CoreMap[cmi][2] = 0;
				CoreMap[cmi][3] = 0;
				PutCoreMap();
			}
		} else {
			if(!err && JobTable[i1].InCore)
				error(32);
			if(!err && CoreAddr < 0)
				error(33);
			if(!err) {
				for(int k1 = 1; k1 <= 21; k1++)
					if(CoreMap[k1][1] != 0 && CoreAddr <= CoreMap[k1][3] && d1 >= (double)CoreMap[k1][2])
						error(34);

			}
			int l1 = 1;
			if(!err)
				for(; l1 <= 21 && CoreMap[l1][1] != 0; l1++);
			cmi = l1;
			if(cmi > 21)
				error(35);
		}
		DrumBusy = true;
		DrmStTm = Clock;
		DrumTimesPtr++;
		Next[3] = DrmTimes[DrumTimesPtr % 10] + Clock;
	}

	static void SnapShot() {
		LastSnap = Clock;
		System.out.println("\n\n\n * * * SYSTEM STATUS AT " + Clock + " * * *");
		System.out.println(" ===================================\n");
		if(Action[0] == 2 && !err13) {
			int i;
			for(i = 1; i <= 21 && (Parms[2] != CoreMap[i][2] || Parms[3] != (CoreMap[i][3] - CoreMap[i][2]) + 1); i++);
			if(i >= 21)
				error(13);
			if(!err13)
				System.out.println(" CPU:  job #" + CoreMap[i][1] + " running");
		} else {
			System.out.println(" CPU:  idle");
		}
		if(!err13) {
			if(DiskBusy) {
				System.out.print(" Disk running for job ");
				System.out.print(JobTable[JobServdIndex].JobNo);
				System.out.println(" since " + DskStTm);
			} else {
				System.out.println(" Disk:  idle");
			}
			if(DrumBusy)
			{
				System.out.print("Drum:  swapping job " + JobSwpdNo);
				if(writ)
					System.out.println(" out since " + DrmStTm);
				else
					System.out.println(" in since " + DrmStTm);
			} else {
				System.out.println(" Drum:  idle");
			}
			int k = coreused();
			System.out.println("Memory:  " + k + " K words in use");
			System.out.print("Average dilation:  ");
			if(DilCntr == 0.0D)
				System.out.println("0.00");
			else
				printf("%.2f\n", AvgDil / DilCntr);
			System.out.print("Average Response time:  ");
			if((double)TermCntr - DilCntr == 0.0D)
				System.out.println("0.00");
			else
				printf("%.2f\n", AvgResponse / ((double)TermCntr - DilCntr));
			boolean flag = trace;
			trace = true;
			PutCoreMap();
			trace = flag;
			System.out.println("\n\n\t\t\tJOBTABLE\n");
			System.out.print("Job#  Size  Time CPUTime MaxCPU  I/O's ");
			System.out.println("Priority Blocked  Latched InCore Term");
			System.out.print("          Arrived  Used  Time   Pending");
			System.out.println("\n\n");
			for(int j = 1; j <= 50; j++)
				if(!JobTable[j].Overwrite) {
					printf("%4d  ", JobTable[j].JobNo);
					printf("%3d  ", JobTable[j].Size);
					printf("%6.0f ", JobTable[j].StartTime);
					printf("%6d ", JobTable[j].CpuTimeUsed);
					printf("%6d  ", JobTable[j].MaxCpuTime);
					printf("%3d  ", JobTable[j].IOPending);
					printf("    %d  ", JobTable[j].Priority);
					if(JobTable[j].Blocked)
						System.out.print("     yes");
					else
						System.out.print("     no ");
					if(JobTable[j].Latched)
						System.out.print("     yes");
					else
						System.out.print("     no ");
					if(JobTable[j].InCore)
						System.out.print("     yes");
					else
						System.out.print("     no ");
					if(JobTable[j].Terminated)
						System.out.println("     yes");
					else
						System.out.println("     no ");
				}

			System.out.println("\n\n");
			if(Clock != 0) {
				System.out.println("\n\n");
				System.out.print(" Total jobs:  " + JobCntr + "\t");
				System.out.println("terminated:  " + TermCntr);
				printf(" %% utilization   CPU:  %.2f", (CpuUtil * 100D) / (double)Clock);
				printf("   disk:  %.2f", (DiskUtil * 100D) / (double)Clock);
				printf("   drum:  %.2f", (DrumUtil * 100D) / (double)Clock);
				printf("   memory:  %.2f", CoreUtil / (double)Clock);
			}
			System.out.println("\n\n");
		}
	}

	private static void Statistics() {
		System.out.println("\n\n                          FINAL STATISTICS");
		SnapShot();
	}

	private static void printf(String s, int i) {
		Format.print(System.out, s, i);
	}

	private static void printf(String s, double d) {
		Format.print(System.out, s, d);
	}

	static final int MAXINT = 0x1312d00;
	static final int Crintt = 1;
	static final int Diskintt = 2;
	static final int Drumintt = 3;
	static final int Troo = 4;
	static final int Svcc = 5;
	static final int MaxJTable = 50;
	static final int MaxDiffJobs = 30;
	static final int TrivCutoff = 100;
	static final int CrMaplmt = 21;
	static final int EndOfDay = 0xdbba0;
	static boolean trace;
	static boolean err;
	static boolean err13;
	static int Clock;
	static int Next[];
	static int Parms[];
	static int jti;
	static int cmi;
	static int Action[];
	static jobtype JobTable[];
	static int CoreMap[][];
	static double DrumUtil;
	static double DiskUtil;
	static double CpuUtil;
	static double CoreUtil;
	static double DelCntr;
	static double DilCntr;
	static int JobCntr;
	static int TermCntr;
	static double AvgDil;
	static double AvgResponse;
	static double AvgDisk;
	static int SizeDist[];
	static int MCpuTimeDist[];
	static int TCpuTimeDist[];
	static int PriorDist[];
	static int Times[][][];
	static int WhichSvc[][];
	static int DrmTimes[];
	static int DskTimes[];
	static int CrdTimes[];
	static int CardTimesPtr;
	static int DiskTimesPtr;
	static int DrumTimesPtr;
	static boolean DiskBusy;
	static boolean DrumBusy;
	static int JobSwpdNo;
	static int CoreAddr;
	static int Sze;
	static boolean writ;
	static int JobServdIndex;
	static int DrmStTm;
	static int DskStTm;
	static double LastSnap;
	static double SpstInt;
	static double LstCrChk;
	static int CondCode;
	static String CondMess[] = {
			"ILLEGAL ERROR", "normally (terminate svc issued) ", "abnormally (max cpu time exceeded) "
	};
	static String errorarray[] = {
			"ILLEGAL ERROR", "/// MAIN ERROR ** INCORRECT VALUE OF ACTION SET BY FIND THE NEXT EVENT ///", "/// MAIN ERROR ** INCORRECT VALUE OF ACTION SET BY GEN ROUTINES ///", "*** MAIN ERROR ** INCORRECT VALUE OF ACTION RETURNED BY OS ***", "*** GENCRINT ERROR ** JOB TABLE FULL - OS PROCESSING JOBS TOO SLOWLY ***", "/// RUN ERROR ** JOB SPECIFIED DOES NOT EXIST IN JOB TABLE ///", "/// RUN ERROR ** JOB SPECIFIED IS NOT IN CORE ///", "/// RUN ERROR ** JOB SPECFIED NOT IN CORE-MISSING IN CORE MAP ///", "*** RUN ERROR ** INCORRECT START LOCATION IN CORE SPECIFIED FOR JOB ***", "*** RUN ERROR ** INCORRECT SIZE IN CORE SPECIFIED FOR JOB ***", 
			"*** RUN ERROR ** JOB SPECIFIED TO RUN IS BLOCKED ***", "*** RUN ERROR ** JOB TERMINATED OR HAS EXCEEDED MAXIMUM CPU TIME ***", "*** RUN ERROR ** QUANTUM OF TIME FOR JOB SPECIFIED EXCEEDED MAXIMUM CPU TIME ***", "*** RUN ERROR ** STARTING ADDRESS OR LENGTH SPECIFIED IS INCORRECT ***", "*** RUN ERROR ** QUANTUM SPECIFIED IS NEGATIVE OR ZERO ***", "*** IDLE ERROR ** UNBLOCKED JOBS EXISTS IN CORE ***", "*** IDLE ERROR ** BLOCKED JOBS IN CORE BUT DISK IDLE ***", "*** IDLE ERROR ** OS FAILS TO SWAP JOBS FROM DRUM INTO EMPTY CORE ***", "*** SIODISK ERROR ** JOB SPECIFIED DOES NOT EXIST ***", "*** SIODISK ERROR ** JOB SPECIFIED NOT IN CORE ***", 
			"*** SIODISK ERROR ** JOB SPECIFIED HAS TERMINATED ***", "*** SIODISK ERROR ** DISK IS BUSY ***", "*** SIODISK ERROR ** JOB HAS NO IO PENDING ***", "*** SIODRUM ERROR ** DRUM IS BUSY ***", "*** SIODRUM ERROR ** JOB SPECIFIED DOES NOT EXIST ***", "*** SIODRUM ERROR ** SIZE OF JOB SPECIFIED IS INCORRECT ***", "*** SIODRUM ERROR ** JOB SPECIFIED HAS TERMINATED ***", "*** SIODRUM ERROR ** JOB SPECIFIED NOT IN CORE ***", "*** SIODRUM ERROR ** JOB SPECIFIED IS LATCHED - IT CANNOT BE SWAPPED ***", "/// SIODRUM ERROR ** JOB SPECIFIED NOT IN CORE - DOES NOT EXIST IN CORE MAP ///", 
			"*** SIODRUM ERROR ** START LOCATION OF JOB SPECIFIED IS INCORRECT ***", "/// SIODRUM ERROR ** SIZE OF JOB SPECIFIED IS INCORRECT ///", "*** SIODRUM ERROR ** JOB SPECIFIED IS ALREADY IN CORE - NO NEED TO SWAP IN ***", "*** SIODRUM ERROR ** START LOCATION OF JOB SPECIFIED IS NEGATIVE ***", "*** SIODRUM ERROR ** CORE ADDRESSES OF JOB SPECIFIED OVERLAP OTHER JOBS ***", "/// SIODRUM ERROR ** CORE MAP FULL - NO ROOM IN CORE ///", "*** SIODRUM ERROR ** ATTEMPT TO SWAP IN JOB WITH SIZE = 0 ***", "/// SAVESTATISTICS ERROR ** JOB SPECIFIED NOT IN CORE MAP ///", "/// GENSVC ERROR ** INCORRECT SWITCH VALUE ///", "/// GENDSKINT ERROR ** CAN'T FIND JOB IN CORE MAP IN ORDER TO DELETE ///", 
			"           "
	};

}
