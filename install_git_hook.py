import os


def des():
    '''

    本地git-hook插件初始化脚本。
    # 创建hooks文件夹
    $ mkdir -p ~/.git-hooksPath/hooks

    # 配置全局
    $ git hooksPath git config --global core.hooksPath ~/.git-hooksPath/hooks
    $ cd ~/.git-hooksPath/hooks
    $ wget  https://seasec.aliyun-inc.com/static/pre-commit
    $ chmod +x pre-commit


    :return:
    '''

# 创建hooks文件夹
os.popen('mkdir -p ~/.git-hooksPath/hooks')

# 检测是否存在pre-commit文件，如果存在先删除

# pre_commit_file = '~/.git-hooksPath/hooks/pre-commit'
# if os.path.exists(pre_commit_file):
os.system('rm -rf ~/.git-hooksPath/hooks/pre-commit')


os.system('rm -rf ~/.git-hooksPath/hooks/pre-push')
# requirements_file = '~/.git-hooksPath/hooks/requirements.txt'
# print(os.path.exists(requirements_file))
# exit(1)
# if os.path.exists(requirements_file):
os.system('rm -rf ~/.git-hooksPath/hooks/requirements.txt')


# 配置全局
os.system('git config --global core.hooksPath ~/.git-hooksPath/hooks')

# 切换到hooks目录wget预检测脚本  # 下载所需的python依赖包
os.system('cd ~/.git-hooksPath/hooks/ && wget https://sea.aliyun-inc.com/static/pre-commit && chmod +x pre-commit && wget https://sea.aliyun-inc.com/static/requirements.txt')

# 下载pre-push 脚本
os.system('cd ~/.git-hooksPath/hooks/ && wget https://sea.aliyun-inc.com/static/pre-push && chmod +x pre-push')

# python安装所需依赖
os.system('pip3 install -r ~/.git-hooksPath/hooks/requirements.txt')
os.system('pip install -r ~/.git-hooksPath/hooks/requirements.txt')

print("安装完成！！")



