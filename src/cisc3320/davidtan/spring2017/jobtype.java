package cisc3320.davidtan.spring2017;
// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   sos.java



public class jobtype {

    public jobtype() {
    }

    public boolean Blocked() {
        return Blocked;
    }

    public void Blocked(boolean flag) {
        Blocked = flag;
    }

    public int CpuTimeUsed() {
        return CpuTimeUsed;
    }

    public void CpuTimeUsed(int i) {
        CpuTimeUsed = i;
    }

    public int IOComp() {
        return IOComp;
    }

    public void IOComp(int i) {
        IOComp = i;
    }

    public int IOPending() {
        return IOPending;
    }

    public void IOPending(int i) {
        IOPending = i;
    }

    public boolean InCore() {
        return InCore;
    }

    public void InCore(boolean flag) {
        InCore = flag;
    }

    public int JobNo() {
        return JobNo;
    }

    public void JobNo(int i) {
        JobNo = i;
    }

    public int JobType()
    {
        return JobType;
    }

    void JobType(int i)
    {
        JobType = i;
    }

    boolean Latched()
    {
        return Latched;
    }

    void Latched(boolean flag)
    {
        Latched = flag;
    }

    int MaxCpuTime()
    {
        return MaxCpuTime;
    }

    void MaxCpuTime(int i)
    {
        MaxCpuTime = i;
    }

    int NextSvc()
    {
        return NextSvc;
    }

    void NextSvc(int i)
    {
        NextSvc = i;
    }

    boolean Overwrite()
    {
        return Overwrite;
    }

    void Overwrite(boolean flag)
    {
        Overwrite = flag;
    }

    int Priority()
    {
        return Priority();
    }

    void Priority(int i)
    {
        Priority = i;
    }

    int Size() {
        return Size;
    }

    void Size(int i) {
        Size = i;
    }

    double StartTime() {
        return StartTime;
    }

    public void StartTime(double d) {
        StartTime = d;
    }

    public int TermCpuTime() {
        return TermCpuTime;
    }

    void TermCpuTime(int i)
    {
        TermCpuTime = i;
    }

    boolean Terminated()
    {
        return Terminated;
    }

    public void Terminated(boolean flag) {
        Terminated = flag;
    }

    /* Variables */
    public int JobNo;
    public int Size;
    public double StartTime;
    public int CpuTimeUsed;
    public int MaxCpuTime;
    public int TermCpuTime;
    public int NextSvc;
    public int IOPending;
    public int IOComp;
    public int Priority;
    public int JobType;
    public boolean Blocked;
    public boolean Latched;
    public boolean InCore;
    public boolean Terminated;
    public boolean Overwrite;
}
