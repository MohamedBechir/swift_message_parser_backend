package swiftparser.messageParsing.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import com.fasterxml.jackson.annotation.JsonRootName;

@Entity
@JsonRootName("Block2")
public class Block2 extends AbstractSwiftBlock{

    @Column
    private String messagePriority = "N";
    @Column
    private String messageType = null;
    @Column
    private String blockName;

    public Block2(){
        super();
    }

    public Integer getNumber() {
        return 2;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessagePriority() {
        return messagePriority;
    }

    public void setMessagePriority(String messagePriority) {
        this.messagePriority = messagePriority;
    }

    public boolean isInput(){
        return true;
    }

    public boolean isOutput(){
        return false;
    }


    public String getBlockName() {
        return this.blockName;
    }

    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }

}
