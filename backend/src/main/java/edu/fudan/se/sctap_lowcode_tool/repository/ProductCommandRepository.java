package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.ProductCommandId;
import edu.fudan.se.sctap_lowcode_tool.model.ProductFunctionCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductCommandRepository extends JpaRepository<ProductFunctionCommand, ProductCommandId> {

    /**
     * 根据 productId 查询所有指令
     */
    List<ProductFunctionCommand> findByProductId(String productId);
    ProductFunctionCommand findByProductIdAndFunctionNameAndCommandName(
            String productId, String functionName, String commandName);
}