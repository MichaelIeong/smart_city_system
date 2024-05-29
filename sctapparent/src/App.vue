<template>
  <div id="app">
    <div class="frame-container" ref="frameContainer">
      <iframe src="http://localhost:8082" class="project-frame" ref="leftFrame"></iframe>
      <div class="divider" ref="divider">
        <div class="drag-button" ref="dragButton">⇔</div>
      </div>
      <iframe src="http://localhost:8083" class="project-frame" ref="rightFrame"></iframe>
    </div>
  </div>
</template>

<script>
export default {
  name: 'App',
  mounted() {
    const dragButton = this.$refs.dragButton;
    const frameContainer = this.$refs.frameContainer;
    const leftFrame = this.$refs.leftFrame;
    const rightFrame = this.$refs.rightFrame;

    let isDragging = false;

    const onMouseMove = (e) => {
      if (!isDragging) return;

      const containerRect = frameContainer.getBoundingClientRect();
      const totalWidth = containerRect.width;
      const leftWidth = e.clientX - containerRect.left;

      // 确保左侧宽度和右侧宽度有最小值，以避免过小
      const minWidthPercentage = 10; // 10% 最小宽度
      const leftWidthPercentage = Math.max((leftWidth / totalWidth) * 100, minWidthPercentage);
      const rightWidthPercentage = Math.max(100 - leftWidthPercentage, minWidthPercentage);

      leftFrame.style.width = `${leftWidthPercentage}%`;
      rightFrame.style.width = `${rightWidthPercentage}%`;
    };

    const onMouseUp = () => {
      isDragging = false;
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
    };

    dragButton.addEventListener('mousedown', () => {
      isDragging = true;
      document.addEventListener('mousemove', onMouseMove);
      document.addEventListener('mouseup', onMouseUp);
    });
  },
};
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  width: 100%;
  margin: 0;
}

#app {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.frame-container {
  display: flex;
  flex: 1;
  width: 100%;
  height: 100%;
}

.project-frame {
  height: 100%;
  border: none;
  width: 50%; /* 默认宽度为50% */
}

.divider {
  width: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #000;
  position: relative;
  z-index: 1;
}

.drag-button {
  width: 20px;
  height: 20px;
  background-color: #fff;
  border: 1px solid #000;
  cursor: ew-resize;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
