// RegisterPage.jsx – Registration with strong password and biometric setup
import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  Alert,
  ActivityIndicator,
  Image,
  KeyboardAvoidingView,
  Platform,
  Switch,
} from 'react-native';
import { useAuth } from './contexts/AuthContext';
import { useTranslation } from './LanguageContext';
import { Eye, EyeOff, Fingerprint } from 'lucide-react-native';
import { NativeModules } from 'react-native';

const { BiometricAuthHelper } = NativeModules;

export default function RegisterPage({ navigation }) {
  const { register } = useAuth();
  const { t } = useTranslation();

  const [form, setForm] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  });
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loading, setLoading] = useState(false);
  const [biometricAvailable, setBiometricAvailable] = useState(false);
  const [biometricType, setBiometricType] = useState('');
  const [enableBiometric, setEnableBiometric] = useState(false);
  const [passwordStrength, setPasswordStrength] = useState(0);

  // Check biometric availability
  useEffect(() => {
    if (BiometricAuthHelper) {
      BiometricAuthHelper.isBiometricAvailable()
        .then((result) => {
          setBiometricAvailable(result.available);
          setBiometricType(result.biometryType || 'biometric');
        })
        .catch(() => setBiometricAvailable(false));
    }
  }, []);

  // Password strength checker
  const checkPasswordStrength = (pass) => {
    let score = 0;
    if (pass.length >= 8) score++;
    if (/[A-Z]/.test(pass)) score++;
    if (/[a-z]/.test(pass)) score++;
    if (/[0-9]/.test(pass)) score++;
    if (/[!@#$%^&*(),.?":{}|<>]/.test(pass)) score++;
    setPasswordStrength(score);
    return score >= 4; // strong enough
  };

  const validateForm = () => {
    if (!form.username || !form.email || !form.password || !form.confirmPassword) {
      Alert.alert(t('error'), t('fill_all_fields'));
      return false;
    }
    if (!form.email.includes('@')) {
      Alert.alert(t('error'), t('invalid_email'));
      return false;
    }
    if (!checkPasswordStrength(form.password)) {
      Alert.alert(
        t('weak_password'),
        t('password_requirements') || 'Min 8 chars, upper, lower, number, special'
      );
      return false;
    }
    if (form.password !== form.confirmPassword) {
      Alert.alert(t('error'), t('passwords_dont_match'));
      return false;
    }
    return true;
  };

  const handleRegister = async () => {
    if (!validateForm()) return;

    setLoading(true);
    try {
      // Step 1: Create account
      const regResult = await register({
        username: form.username,
        email: form.email,
        password: form.password,
      });

      if (regResult.success) {
        // Step 2: If biometric enabled, save biometric preference
        if (enableBiometric && biometricAvailable) {
          // Authenticate to confirm it's the user
          const bioResult = await BiometricAuthHelper.authenticate({
            title: t('enable_biometric'),
            subtitle: t('register_biometric_sub'),
            description: t('register_biometric_desc'),
          });
          if (bioResult.success) {
            // Save biometric preference in user profile (call API)
            await regResult.user.updateBiometric(true);
            Alert.alert(t('success'), t('biometric_enabled'));
          } else {
            Alert.alert(t('error'), t('biometric_failed'));
          }
        }

        Alert.alert(t('success'), t('registration_success'));
        navigation?.replace('Home');
      } else {
        Alert.alert(t('error'), regResult.message || t('registration_failed'));
      }
    } catch (error) {
      Alert.alert(t('error'), error.message || t('something_wrong'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
      className="flex-1 bg-[#090d16] justify-center px-6"
    >
      <View className="items-center mb-8">
        <Image
          source={require('../assets/shebaodds_logo.png')}
          style={{ width: 180, height: 60, resizeMode: 'contain' }}
        />
        <Text className="text-amber-400 text-lg font-bold mt-2">Smart Bets. Real Wins.</Text>
      </View>

      <View className="bg-[#111625] p-6 rounded-2xl border border-slate-800">
        <Text className="text-white text-2xl font-bold mb-2">{t('create_account')}</Text>
        <Text className="text-slate-400 mb-6">{t('register_sub') || 'Join us and start winning'}</Text>

        {/* Username */}
        <View className="mb-3">
          <Text className="text-slate-300 text-sm mb-1">{t('username')}</Text>
          <TextInput
            className="bg-[#090d16] border border-slate-800 rounded-lg px-4 py-3 text-white"
            placeholder={t('choose_username')}
            placeholderTextColor="#64748b"
            value={form.username}
            onChangeText={(text) => setForm({ ...form, username: text })}
            autoCapitalize="none"
          />
        </View>

        {/* Email */}
        <View className="mb-3">
          <Text className="text-slate-300 text-sm mb-1">{t('email')}</Text>
          <TextInput
            className="bg-[#090d16] border border-slate-800 rounded-lg px-4 py-3 text-white"
            placeholder="you@example.com"
            placeholderTextColor="#64748b"
            value={form.email}
            onChangeText={(text) => setForm({ ...form, email: text })}
            keyboardType="email-address"
            autoCapitalize="none"
          />
        </View>

        {/* Password */}
        <View className="mb-3">
          <Text className="text-slate-300 text-sm mb-1">{t('password')}</Text>
          <View className="flex-row items-center bg-[#090d16] border border-slate-800 rounded-lg">
            <TextInput
              className="flex-1 px-4 py-3 text-white"
              placeholder={t('enter_password')}
              placeholderTextColor="#64748b"
              secureTextEntry={!showPassword}
              value={form.password}
              onChangeText={(text) => {
                setForm({ ...form, password: text });
                checkPasswordStrength(text);
              }}
            />
            <TouchableOpacity onPress={() => setShowPassword(!showPassword)} className="pr-3">
              {showPassword ? <EyeOff size={20} color="#64748b" /> : <Eye size={20} color="#64748b" />}
            </TouchableOpacity>
          </View>
          {/* Strength indicator */}
          <View className="flex-row mt-1 space-x-1">
            {[1,2,3,4,5].map((level) => (
              <View
                key={level}
                className={`h-1 flex-1 rounded-full ${
                  level <= passwordStrength
                    ? passwordStrength >= 4
                      ? 'bg-green-500'
                      : 'bg-amber-500'
                    : 'bg-slate-700'
                }`}
              />
            ))}
          </View>
          <Text className="text-xs text-slate-500 mt-1">
            {t('password_requirements') || 'Min 8 chars, upper, lower, number, special'}
          </Text>
        </View>

        {/* Confirm Password */}
        <View className="mb-4">
          <Text className="text-slate-300 text-sm mb-1">{t('confirm_password')}</Text>
          <View className="flex-row items-center bg-[#090d16] border border-slate-800 rounded-lg">
            <TextInput
              className="flex-1 px-4 py-3 text-white"
              placeholder={t('confirm_password_placeholder')}
              placeholderTextColor="#64748b"
              secureTextEntry={!showConfirm}
              value={form.confirmPassword}
              onChangeText={(text) => setForm({ ...form, confirmPassword: text })}
            />
            <TouchableOpacity onPress={() => setShowConfirm(!showConfirm)} className="pr-3">
              {showConfirm ? <EyeOff size={20} color="#64748b" /> : <Eye size={20} color="#64748b" />}
            </TouchableOpacity>
          </View>
        </View>

        {/* Biometric toggle (if available) */}
        {biometricAvailable && (
          <View className="flex-row items-center justify-between mb-4 p-3 bg-[#090d16] rounded-lg border border-slate-800">
            <View className="flex-row items-center">
              <Fingerprint size={20} color="#f0b90b" />
              <Text className="text-slate-300 ml-3 text-sm">
                {t('enable_biometric')} ({biometricType})
              </Text>
            </View>
            <Switch
              value={enableBiometric}
              onValueChange={setEnableBiometric}
              trackColor={{ false: '#475569', true: '#f0b90b' }}
              thumbColor={enableBiometric ? '#f0b90b' : '#f4f4f4'}
            />
          </View>
        )}

        {/* Register button */}
        <TouchableOpacity
          className={`bg-amber-500 py-4 rounded-lg items-center ${loading ? 'opacity-50' : ''}`}
          onPress={handleRegister}
          disabled={loading}
        >
          {loading ? (
            <ActivityIndicator color="#090d16" />
          ) : (
            <Text className="text-[#090d16] font-bold text-base">{t('register') || 'Create Account'}</Text>
          )}
        </TouchableOpacity>

        {/* Already have account */}
        <View className="flex-row justify-center mt-4">
          <Text className="text-slate-400 text-sm">{t('have_account') || 'Already have an account?'}</Text>
          <TouchableOpacity onPress={() => navigation?.navigate('Login')}>
            <Text className="text-amber-400 text-sm font-semibold ml-1">{t('login') || 'Sign In'}</Text>
          </TouchableOpacity>
        </View>
      </View>

      <Text className="text-slate-500 text-xs text-center mt-6">
        {t('secure_register_note') || '🔒 Your data is SSL encrypted and securely stored'}
      </Text>
    </KeyboardAvoidingView>
  );
}