package net.zhenhuojun.spellweaver.spell;

import java.util.ArrayList;
import java.util.List;

//符文寄存器的实现类
public class RuneRegister {
    List<String> spellList=new ArrayList<>();

    public RuneRegister(List spellList){
        this.spellList=spellList;
    }

    public List<String> getSpellList() {
        return spellList;
    }
}
