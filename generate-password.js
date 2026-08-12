// 生成BCrypt加密密码的脚本
// 在Node.js环境中运行: node generate-password.js

const bcrypt = require('bcrypt');

const password = 'admin123';
const saltRounds = 10;

bcrypt.hash(password, saltRounds, function(err, hash) {
    if (err) {
        console.error('Error:', err);
        return;
    }
    console.log('Password:', password);
    console.log('BCrypt Hash:', hash);
    
    // 生成SQL语句
    console.log('\nSQL语句:');
    console.log(`INSERT INTO user (username, password, email, role) VALUES ('admin', '${hash}', 'admin@example.com', 'ADMIN');`);
});
