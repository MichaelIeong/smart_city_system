<template>
    <div class="main">
        <div id="hello">Hello {{ user }}</div>
        <div id="two-way-binding">
            <p>{{ message }}</p>
            <input v-model="message">
        </div>
        <div id="example1" class="demo">
            <p> Using mustaches:{{ rawHtml }}</p>
            <p> Using v-html directive:<span v-html="rawHtml"></span></p>
        </div>
        <div :id="test1id">
            <p>{{ test1id }}</p>
        </div>
        <button :disabled="isButtonDisabled" @click="changetest1()">Button</button>

    </div>
</template>
<script>
export default {
    data () {
        return {
            user: 'mixiaochao',
            message: 'please input your msg.',
            rawHtml: '<span style="color:red">This should be red.</span>',
            isButtonDisabled: false,
            test1id: 'test1'
        }
    },
    methods: {
        changetest1 () {
           this.test1id = 'test2'
        }
    }
}
</script>

<style lang="less" scoped>
  #hello {
    color: red;
    font-size: 40px;
  }
  #test1 {
    color: blue;
    font-size: 40px;
  }
  #test2 {
    color: green;
    font-size: 40px;
  }
</style>
