package edu.fudan.se.sctap_lowcode_tool.repository;

import edu.fudan.se.sctap_lowcode_tool.model.ProductCommandId;
import edu.fudan.se.sctap_lowcode_tool.model.ProductFunctionCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface ProductCommandRepository extends JpaRepository<ProductFunctionCommand, ProductCommandId> {

    /**
     * 根据 productId 查询所有指令
     */
    List<ProductFunctionCommand> findByProductId(String productId);

    /**
     * 根据 product_id 查询对应的功能名称列表
     * @param productId 产品 ID (例如: p_ai_camera_tst)
     * @return 包含 function_name 的字符串列表
     */
    @Query("SELECT DISTINCT p.functionName FROM ProductFunctionCommand p WHERE p.productId = ?1")
    List<String> getFunctionsByProductId(String productId);

    @Query("SELECT DISTINCT p.commandName FROM ProductFunctionCommand p WHERE p.functionName = ?1")
    List<String> getCommandsNameByFunction(String functionName);

    @Query("SELECT DISTINCT p.commandJson FROM ProductCommandJson p WHERE p.commandId = ?1")
    String getCommandJsonById(String commandId);
    ProductFunctionCommand findByProductIdAndFunctionNameAndCommandName(
            String productId, String functionName, String commandName);
}