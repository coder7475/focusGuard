export const openDatabaseAsync = jest.fn(async () => ({
  execAsync: jest.fn(),
  runAsync: jest.fn(),
  getFirstAsync: jest.fn(),
  getAllAsync: jest.fn(),
}));
