/** @type {import('ts-jest').JestConfigWithTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testMatch: ['**/__tests__/**/*.test.ts'],
  transform: {
    '^.+\\.tsx?$': ['ts-jest', { diagnostics: false }],
  },
  moduleNameMapper: {
    '^expo-sqlite$': '<rootDir>/src/data/__mocks__/expo-sqlite.ts',
    '^expo-modules-core$': '<rootDir>/src/data/__mocks__/expo-modules-core.ts',
    '^@/(.*)$': '<rootDir>/src/$1',
  },
};
