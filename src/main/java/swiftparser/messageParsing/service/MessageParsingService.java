package swiftparser.messageParsing.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.prowidesoftware.swift.model.SwiftBlock1;
import com.prowidesoftware.swift.model.SwiftBlock2;
import com.prowidesoftware.swift.model.SwiftBlock3;
import com.prowidesoftware.swift.model.SwiftBlock4;
import com.prowidesoftware.swift.model.SwiftBlock5;
import com.prowidesoftware.swift.model.SwiftMessage;
import com.prowidesoftware.swift.model.SwiftTagListBlock;
import com.prowidesoftware.swift.model.Tag;
import com.prowidesoftware.swift.model.mt.AbstractMT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import swiftparser.messageParsing.model.AbstractBlockField;
import swiftparser.messageParsing.model.AbstractSwiftMessage;
import swiftparser.messageParsing.model.Block1;
import swiftparser.messageParsing.model.Block2;
import swiftparser.messageParsing.model.TagBlock;
import swiftparser.messageParsing.repository.BlockRepository;
import swiftparser.messageParsing.repository.MessageRepository;
import swiftparser.messageParsing.repository.TagBlockRepository;

@Service
public class MessageParsingService {

    @Autowired
    private MessageStorageService messageStorageService;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TagBlockRepository tagBlockRepository;


    List<TagBlock> tagBlocksList = new ArrayList<>();
    List<AbstractBlockField> fieldsList;
    Set<AbstractBlockField> fieldsSet;
    TagBlock tagBlock;

    public void storeBlock(SwiftTagListBlock swiftBlock, AbstractSwiftMessage message) {
        // Create Tag Block Instance
        tagBlock = new TagBlock();
        // Assign name to the block
        if (swiftBlock != null) {
            String blockName = swiftBlock.getName();
            if (blockName.equals("3")) {
                tagBlock.setBlockName("User Header Block");
            } else if(blockName.equals("4")) {
                tagBlock.setBlockName("Text Block");
            }else if(blockName.equals("5")){
                tagBlock.setBlockName("Tail Block");
            }

            // Create List of Tag Block Fields
            fieldsList = new ArrayList<>();

            // Assign the Tag Name and Value to the Block Field
            for (Tag tag : swiftBlock.getTags()) {
                AbstractBlockField abstractBlockField = new AbstractBlockField();
                abstractBlockField.setTagName(tag.getName());
                abstractBlockField.setTagValue(tag.getValue());
                fieldsList.add(abstractBlockField);
            }

            fieldsSet = new HashSet<>();
            fieldsSet.addAll(fieldsList);
            tagBlock.setBlockNumber(swiftBlock.getNumber());
            tagBlock.setFields(fieldsSet);
            tagBlockRepository.save(tagBlock);
            tagBlocksList.add(tagBlock);
        }
    }

    public String decomposeMessage(Long id) throws IOException {

        // Get the message by its ID
        AbstractSwiftMessage abstractSwiftMessage = messageRepository.findById(id).get();
        // Read the SWIFT Message as a String
        String message = messageStorageService.readMessage(id);
        // Parse the SWIFT Message to an Abstract MT Message
        AbstractMT msg = AbstractMT.parse("File size is: " + message);
        SwiftMessage swiftMessage = msg.getSwiftMessage();
        
        /*
        Swift Block 1
        */
        Block1 block1 = new Block1();

        SwiftBlock1 swiftBlock1 = swiftMessage.getBlock1();

        block1.setApplicationId(swiftBlock1.getApplicationId());
        block1.setLogicalTerminal(swiftBlock1.getLogicalTerminal());
        block1.setServiceId(swiftBlock1.getServiceId());
        block1.setSessionNumber(swiftBlock1.getSessionNumber());
        block1.setSequenceNumber(swiftBlock1.getSequenceNumber());
        block1.setBlockName("Basic Header Block");
        blockRepository.save(block1);
        abstractSwiftMessage.setBlock1(block1);

        /*
        Swift Block 2
        */
        Block2 block2 = new Block2();

        SwiftBlock2 swiftBlock2 = swiftMessage.getBlock2();

        block2.setMessagePriority(swiftBlock2.getMessagePriority());
        if(swiftBlock2.getMessagePriority().equals("S")){
            block2.setMessagePriority("System");
        }else if (swiftBlock2.getMessagePriority().equals("N")){
            block2.setMessagePriority("Normal");
        }else if (swiftBlock2.getMessagePriority().equals("U")){
            block2.setMessagePriority("Urgent");
        }
        block2.setMessageType(swiftBlock2.getMessageType());
        block2.setBlockName("Application Header Block");
        if(swiftBlock2.isInput()){
            block2.setMessageDirection("Input");
        }else{
            block2.setMessageDirection("Output");
        }
        blockRepository.save(block2);
        abstractSwiftMessage.setBlock2(block2);
        /*
        Swift Tag Block(Blocks 3, 4, 5)
        */

       SwiftBlock3 swiftBlock3 = swiftMessage.getBlock3();
        SwiftBlock4 swiftBlock4 = swiftMessage.getBlock4();
        SwiftBlock5 swiftBlock5 = swiftMessage.getBlock5();
        
        storeBlock(swiftBlock3, abstractSwiftMessage);
        storeBlock(swiftBlock4, abstractSwiftMessage);
        storeBlock(swiftBlock5, abstractSwiftMessage);

        Set<TagBlock> tagBlocksSet = new HashSet<>();
        tagBlocksSet.addAll(tagBlocksList);
        tagBlocksList.clear();
        abstractSwiftMessage.setTagBlock(tagBlocksSet);
        messageRepository.save(abstractSwiftMessage);

        return "Success";
    }
}
