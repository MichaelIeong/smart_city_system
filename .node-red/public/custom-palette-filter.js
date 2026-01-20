(function() {
    function doFilter() {
        const urlParams = new URLSearchParams(window.location.search);
        const type = urlParams.get('type');
        if (!type) return;

        // 这里的 key 对应 URL 的 type，value 对应 div ID 中最后的部分
        const mapping = {
            "1": ["应用构造"],
            "2": ["事件融合"],
            "3": ["服务组合"]
        };

        const allowed = mapping[type];
        if (!allowed) return;

        $(".red-ui-palette-category").each(function() {
            const id = $(this).attr('id') || "";
            // 提取 ID 末尾的名字，例如 red-ui-palette-container-network -> network
            const categoryId = id.replace("red-ui-palette-container-", "");

            const shouldShow = allowed.includes(categoryId);

            if (shouldShow) {
                $(this).show();
                // 强制移除 Node-RED 可能带有的隐藏类
                $(this).removeClass("hide");
            } else {
                $(this).hide();
            }
        });
    }

    // 每秒轮询，解决 Node-RED 动态加载问题
    setInterval(doFilter, 500);
    console.log("=== Palette 过滤器已通过 ID 匹配模式启动 ===");
})();